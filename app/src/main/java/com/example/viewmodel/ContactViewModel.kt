package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Contact
import com.example.data.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val searchQuery = MutableStateFlow("")

    val contacts: StateFlow<List<Contact>>

    // Selected contact for detail view / quick action
    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    init {
        val contactDao = AppDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)

        // Seed default template contact if database is empty on launch
        viewModelScope.launch {
            try {
                repository.getAllContacts().first().let { list ->
                    if (list.isEmpty()) {
                        repository.insert(
                            Contact(
                                name = "Silmar Vargas",
                                phoneNumber = "11999992233",
                                email = "br.silmarvargas@gmail.com",
                                bank = "Inter",
                                pixKey = "03733876016",
                                notes = "Usuário modelo com informações de banco e chave Pix CPF cadastradas."
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore seeding errors
            }
        }

        contacts = searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.getAllContacts()
                } else {
                    repository.searchContacts(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    fun search(query: String) {
        searchQuery.value = query
    }

    fun insertContact(contact: Contact) {
        viewModelScope.launch {
            repository.insert(contact)
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.update(contact)
            // If the currently selected contact is the one being updated, refresh it
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = contact
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.delete(contact)
            if (_selectedContact.value?.id == contact.id) {
                _selectedContact.value = null
            }
        }
    }

    /**
     * Send message via WhatsApp
     */
    fun sendWhatsApp(context: Context, rawPhone: String, message: String) {
        // Remove non-digits
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")

        // Support standard Brazilian format:
        // Brazilian phone number has 10/11 digits (e.g., DDD + phone).
        // If it does not start with 55 (Brazil country code), add it automatically.
        if ((cleanPhone.length == 10 || cleanPhone.length == 11) && !cleanPhone.startsWith("55")) {
            cleanPhone = "55$cleanPhone"
        }

        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "Telefone inválido para envio.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        
        // Attempt WhatsApp package direct call, fallback to browser
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val whatsappInstalled = try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0) // WhatsApp Business
                true
            } catch (err: Exception) {
                false
            }
        }

        if (whatsappInstalled) {
            // First try WhatsApp general package, fallback if anything goes wrong
            try {
                val waIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    `package` = "com.whatsapp"
                }
                context.startActivity(waIntent)
            } catch (ex: Exception) {
                try {
                    val waBusinessIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                        `package` = "com.whatsapp.w4b"
                    }
                    context.startActivity(waBusinessIntent)
                } catch (ex2: Exception) {
                    context.startActivity(intent)
                }
            }
        } else {
            // Fallback for general browser
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Nenhum aplicativo para abrir o WhatsApp foi encontrado.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class ContactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
