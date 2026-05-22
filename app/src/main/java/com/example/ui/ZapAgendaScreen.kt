package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Contact
import com.example.viewmodel.ContactViewModel

@Composable
fun getInitialColors(char: Char): Pair<Color, Color> {
    val uppercaseChar = char.uppercaseChar()
    return when (uppercaseChar) {
        in 'A'..'E' -> Pair(Color(0xFFEADDFF), Color(0xFF21005D)) // Standard Lavender
        in 'F'..'J' -> Pair(Color(0xFFF7D8FF), Color(0xFF4A0072)) // Light plum
        in 'K'..'O' -> Pair(Color(0xFFFFD8D8), Color(0xFF680003)) // Warm peach
        in 'P'..'T' -> Pair(Color(0xFFD1E3FF), Color(0xFF001D35)) // Soft blue
        else -> Pair(Color(0xFFFFE082), Color(0xFF7F6D00)) // Pastel gold
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapAgendaScreen(
    viewModel: ContactViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ZapAgenda",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // High fidelity bottom bar to match the "Clean Minimalism" layout structure exactly
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                tonalElevation = 0.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on main agenda page */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Agenda") },
                    label = { Text("Agenda", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        Toast.makeText(context, "Consultando todos os contatos salvos", Toast.LENGTH_SHORT).show()
                    },
                    icon = { Icon(Icons.Outlined.Refresh, contentDescription = "Recentes") },
                    label = { Text("Recentes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        Toast.makeText(context, "ZapAgenda v1.2 - Design Minimalista M3", Toast.LENGTH_SHORT).show()
                    },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    contactToEdit = null
                    showAddDialog = true
                },
                containerColor = Color(0xFFD3E3FD), // Cool blue container from "Clean Minimalism"
                contentColor = Color(0xFF001D35), // Dark text/icon color
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("add_contact_fab")
                    .padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contato")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Lupa & Search Bar Area
            SearchArea(
                query = searchQuery,
                onQueryChange = { viewModel.search(it) },
                onClearQuery = { viewModel.search("") },
                totalCount = contacts.size
            )

            // Dynamic Uppercase Category Section Label from Clean Minimalism Design
            PaddingValues(horizontal = 24.dp, vertical = 8.dp).let { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    Text(
                        text = if (searchQuery.isEmpty()) "CONTATOS SALVOS" else "RESULTADOS DA BUSCA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (contacts.isEmpty()) {
                    EmptyStateView(isSearching = searchQuery.isNotEmpty())
                } else {
                    ContactList(
                        contacts = contacts,
                        selectedContact = selectedContact,
                        onContactClick = { contact ->
                            if (selectedContact?.id == contact.id) {
                                viewModel.selectContact(null)
                            } else {
                                viewModel.selectContact(contact)
                            }
                        },
                        onQuickWhatsApp = { contact ->
                            viewModel.sendWhatsApp(
                                context = context,
                                rawPhone = contact.phoneNumber,
                                message = "Olá ${contact.name}! Como vai?"
                            )
                        }
                    )
                }
            }

            // Quick consulting & WhatsApp composer drawer
            AnimatedVisibility(
                visible = selectedContact != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring()) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                selectedContact?.let { contact ->
                    ContactDetailsSheet(
                        contact = contact,
                        onClose = { viewModel.selectContact(null) },
                        onEdit = {
                            contactToEdit = contact
                            showAddDialog = true
                        },
                        onDelete = {
                            viewModel.deleteContact(contact)
                            Toast.makeText(context, "Contato removido", Toast.LENGTH_SHORT).show()
                        },
                        onSendWhatsApp = { message ->
                            viewModel.sendWhatsApp(context, contact.phoneNumber, message)
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Dialog Form
    if (showAddDialog) {
        AddEditContactDialog(
            contact = contactToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, email, bank, pixKey, notes ->
                if (contactToEdit == null) {
                    viewModel.insertContact(
                        Contact(
                            name = name,
                            phoneNumber = phone,
                            email = email,
                            bank = bank,
                            pixKey = pixKey,
                            notes = notes
                        )
                    )
                    Toast.makeText(context, "Contato salvo com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateContact(
                        contactToEdit!!.copy(
                            name = name,
                            phoneNumber = phone,
                            email = email,
                            bank = bank,
                            pixKey = pixKey,
                            notes = notes
                        )
                    )
                    Toast.makeText(context, "Contato atualizado!", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SearchArea(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // High fidelity capsule matching "flex items-center bg-[#f3edf7] h-14 px-4 rounded-full shadow-sm"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Lupa de pesquisa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
                
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input"),
                    placeholder = {
                        Text(
                            "Consultar usuário...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onClearQuery) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpar busca",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )

                // Personal branding avatar matching "w-10 h-10 rounded-full bg-[#6750a4]"
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SV", // Silmar Vargas
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isEmpty()) "Todos os contatos" else "Resultado da busca",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "$totalCount " + if (totalCount == 1) "salvo" else "salvos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ContactList(
    contacts: List<Contact>,
    selectedContact: Contact?,
    onContactClick: (Contact) -> Unit,
    onQuickWhatsApp: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            val isSelected = selectedContact?.id == contact.id
            ContactCard(
                contact = contact,
                isSelected = isSelected,
                onClick = { onContactClick(contact) },
                onQuickWhatsApp = { onQuickWhatsApp(contact) }
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    isSelected: Boolean,
    onClick: () -> Unit,
    onQuickWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = contact.name.trim().firstOrNull()?.uppercase() ?: "?"
    val colorPair = getInitialColors(contact.name.firstOrNull() ?: ' ')

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("contact_item_card_${contact.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(20.dp) // Beautiful rounded-2xl look
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alternating color avatar with depth and modern style
                Box(
                    modifier = Modifier
                        .size(48.dp) // Perfect w-12 h-12 in design
                        .clip(CircleShape)
                        .background(colorPair.first),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPair.second
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    if (contact.bank.isNotEmpty() || contact.pixKey.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (contact.bank.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = contact.bank,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            if (contact.pixKey.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFDCF8C6))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Pix: ${contact.pixKey}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF075E54),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (contact.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = contact.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick send WhatsApp trigger matching the gorgeous emerald bubble styling:bg-[#dcf8c6] text-[#075e54]
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCF8C6)) // Retro conver green background
                    .clickable(onClick = onQuickWhatsApp)
                    .testTag("quick_whatsapp_${contact.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Zapear Rápido",
                    tint = Color(0xFF075E54), // Deep retro teal text color
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 1.dp) // Subtle aesthetic alignment offset
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Search else Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearching) "Nenhum resultado encontrado" else "Sua agenda está vazia!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isSearching) {
                "Tente buscar usando outro nome ou número."
            } else {
                "Adicione contatos clicando no botão verde abaixo para gerenciar e enviar mensagens rápidas."
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ContactDetailsSheet(
    contact: Contact,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendWhatsApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Dynamic WhatsApp preset formatting matching user layout requirements exactly
    val presets = remember(contact) {
        val list = mutableListOf(
            "Olá ${contact.name}! Como está?",
            "Oi, passando para confirmar as informações solicitadas."
        )
        if (contact.pixKey.isNotEmpty()) {
            val pixMessage = "*Nome:* ${contact.name}\n" +
                    (if (contact.bank.isNotEmpty()) "*Banco:* ${contact.bank}\n" else "") +
                    "*Chave Pix:* ${contact.pixKey}"
            list.add(0, pixMessage) // Place at index 0 for instant access
        }
        list
    }

    var messageText by remember(contact.id) { 
        mutableStateOf(
            if (contact.pixKey.isNotEmpty()) {
                "*Nome:* ${contact.name}\n" +
                (if (contact.bank.isNotEmpty()) "*Banco:* ${contact.bank}\n" else "") +
                "*Chave Pix:* ${contact.pixKey}"
            } else {
                "Olá ${contact.name}! Como está?"
            }
        ) 
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contact_detail_panel"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consulta de Contato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Fechar"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details info
            Text(
                text = contact.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Telefone",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (contact.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "E-mail",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = contact.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bancos & Pix Visual Cards
            if (contact.bank.isNotEmpty() || contact.pixKey.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (contact.bank.isNotEmpty()) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "BANCO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = contact.bank,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (contact.pixKey.isNotEmpty()) {
                        Card(
                            modifier = Modifier.weight(1.3f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CHAVE PIX",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = contact.pixKey,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Chave Pix", contact.pixKey)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Chave Pix copiada!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Copiar Pix",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (contact.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = contact.notes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message Composer area
            Text(
                text = "Mensagem para o WhatsApp:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Horizontal Presets Scroller
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(presets) { preset ->
                    AssistChip(
                        onClick = { messageText = preset },
                        label = {
                            Text(
                                text = preset.replace("\n", " "),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inline Composer Box
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("whatsapp_message_input"),
                placeholder = { Text("Mensagem personalizada...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Send Action button
            Button(
                onClick = { onSendWhatsApp(messageText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_whatsapp_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zapear Agora",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun AddEditContactDialog(
    contact: Contact?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, bank: String, pixKey: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var bank by remember { mutableStateOf(contact?.bank ?: "") }
    var pixKey by remember { mutableStateOf(contact?.pixKey ?: "") }
    var notes by remember { mutableStateOf(contact?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_edit_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (contact == null) "Novo Contato" else "Editar Contato",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError) nameError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_form_input"),
                    label = { Text("Nome Completo") },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Nome é obrigatório.", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true
                )

                // Telephone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        // Only let digits, spaces, hyphens and parenthesis in phone input
                        phone = input.filter { it.isDigit() || it == '+' || it == '(' || it == ')' || it == '-' || it == ' ' }
                        if (phoneError) phoneError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_form_input"),
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("DDD + Fone (ex: 11999998888)") },
                    supportingText = {
                        if (phoneError) {
                            Text("Verifique o número de telefone.", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Salvaremos localmente para abrir no WhatsApp.", fontSize = 11.sp)
                        }
                    },
                    isError = phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                // Bank Input
                OutlinedTextField(
                    value = bank,
                    onValueChange = { bank = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_form_input"),
                    label = { Text("Banco") },
                    placeholder = { Text("Ex: Inter, Nubank, Itaú, etc.") },
                    singleLine = true
                )

                // Pix Key Input
                OutlinedTextField(
                    value = pixKey,
                    onValueChange = { pixKey = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pix_key_form_input"),
                    label = { Text("Chave Pix CPF / CNPJ / Celular") },
                    placeholder = { Text("Ex: 03733876016") },
                    singleLine = true
                )

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_form_input"),
                    label = { Text("E-mail (Opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                // Notes / Agenda descriptive input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_form_input"),
                    label = { Text("Notas / Observações") },
                    placeholder = { Text("Ex: Cliente de varejo, etc.") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions Cancel/Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_dialog_button")
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = true
                                hasError = true
                            }
                            if (phone.isBlank() || phone.replace(Regex("[^0-9]"), "").isEmpty()) {
                                phoneError = true
                                hasError = true
                            }
                            if (!hasError) {
                                onSave(name.trim(), phone.trim(), email.trim(), bank.trim(), pixKey.trim(), notes.trim())
                            }
                        },
                        modifier = Modifier.testTag("save_contact_button")
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}
