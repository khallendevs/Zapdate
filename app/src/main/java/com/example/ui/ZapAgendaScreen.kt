package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.saveable.rememberSaveable
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    // SharedPreferences settings storage for local configurations
    val sharedPref = remember { context.getSharedPreferences("zap_agenda_prefs", Context.MODE_PRIVATE) }
    var appThemePreset by remember { mutableStateOf(sharedPref.getString("app_theme_preset", "Royal Amethyst") ?: "Royal Amethyst") }
    var notificationsEnabled by remember { mutableStateOf(sharedPref.getBoolean("notifications_enabled", true)) }
    var appPassword by remember { mutableStateOf(sharedPref.getString("app_password", "") ?: "") }
    
    // Lock state on startup - if app password is not empty, start in a locked state
    var isUnlocked by rememberSaveable(appPassword) { mutableStateOf(appPassword.isEmpty()) }
    
    var showSettingsDialog by remember { mutableStateOf(false) }

    val darkTheme = isSystemInDarkTheme()
    val ambientGradient = remember(darkTheme, appThemePreset) {
        val darkThemeColors = when(appThemePreset) {
            "Emerald WhatsApp" -> listOf(Color(0xFF071F11), Color(0xFF03140C), Color(0xFF0B171E))
            "Sunset Gold" -> listOf(Color(0xFF1E140C), Color(0xFF281C0E), Color(0xFF1F0E16))
            "Deep Sapphire" -> listOf(Color(0xFF05172E), Color(0xFF020C1B), Color(0xFF15082E))
            "Cyber Silver" -> listOf(Color(0xFF1A1A1A), Color(0xFF111111), Color(0xFF17191C))
            else -> listOf(Color(0xFF0F0A1C), Color(0xFF070F1E), Color(0xFF081A12)) // Royal Amethyst
        }
        val lightThemeColors = when(appThemePreset) {
            "Emerald WhatsApp" -> listOf(Color(0xFFE9F5EF), Color(0xFFDCF8C6), Color(0xFFF1FDF6))
            "Sunset Gold" -> listOf(Color(0xFFFDF5E6), Color(0xFFFFE4E1), Color(0xFFFAF0E6))
            "Deep Sapphire" -> listOf(Color(0xFFE3F2FD), Color(0xFFE1F5FE), Color(0xFFECEFF1))
            "Cyber Silver" -> listOf(Color(0xFFF0F2F5), Color(0xFFE9ECEF), Color(0xFFF8F9FA))
            else -> listOf(Color(0xFFF1EEFA), Color(0xFFE8F1FC), Color(0xFFE9F4EE)) // Royal Amethyst
        }
        
        Brush.verticalGradient(colors = if (darkTheme) darkThemeColors else lightThemeColors)
    }

    val themeAccentColors = remember(darkTheme, appThemePreset) {
        if (darkTheme) {
            when(appThemePreset) {
                "Emerald WhatsApp" -> listOf(Color(0xFF25D366), Color(0xFF1EBF53))
                "Sunset Gold" -> listOf(Color(0xFFFFB74D), Color(0xFFFFA726))
                "Deep Sapphire" -> listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6))
                "Cyber Silver" -> listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5))
                else -> listOf(Color(0xFFD0BCFF), Color(0xFF25D366)) // Purple & original secondary emerald Green
            }
        } else {
            when(appThemePreset) {
                "Emerald WhatsApp" -> listOf(Color(0xFF128C7E), Color(0xFF25D366))
                "Sunset Gold" -> listOf(Color(0xFFE07A5F), Color(0xFFF2CC8F))
                "Deep Sapphire" -> listOf(Color(0xFF0077B6), Color(0xFF03045E))
                "Cyber Silver" -> listOf(Color(0xFF37474F), Color(0xFF546E7A))
                else -> listOf(Color(0xFF6750A4), Color(0xFF25D366)) // Royal Amethyst
            }
        }
    }

    if (!isUnlocked) {
        AppLockScreen(
            correctPassword = appPassword,
            onUnlock = { isUnlocked = true }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ambientGradient)
        ) {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = themeAccentColors
                                            )
                                        ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ZapAgenda",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                val glassNavBg = if (darkTheme) Color(0x3D1D1B20) else Color(0xB8FFFFFF)
                val glassBorderColor = Color.White.copy(alpha = if (darkTheme) 0.12f else 0.5f)
                
                NavigationBar(
                    containerColor = glassNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(glassBorderColor, Color.Transparent)
                            ),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                            showSettingsDialog = true
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
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .testTag("add_contact_fab")
                        .padding(bottom = 8.dp)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Contato")
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Transparent)
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

    if (showSettingsDialog) {
        SettingsDialog(
            currentTheme = appThemePreset,
            onThemeChange = { newTheme ->
                sharedPref.edit().putString("app_theme_preset", newTheme).apply()
                appThemePreset = newTheme
            },
            notificationsEnabled = notificationsEnabled,
            onNotificationsToggle = { enabled ->
                sharedPref.edit().putBoolean("notifications_enabled", enabled).apply()
                notificationsEnabled = enabled
                val msg = if (enabled) "Notificações ativadas!" else "Notificações desativadas!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            appPassword = appPassword,
            onPasswordSave = { newPass ->
                sharedPref.edit().putString("app_password", newPass).apply()
                appPassword = newPass
                val msg = if (newPass.isEmpty()) "Senha de bloqueio removida!" else "Nova senha registrada!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            contactsList = contacts,
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
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
    val darkTheme = isSystemInDarkTheme()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // High fidelity glassy capsule matching polished floating glass panels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(if (darkTheme) Color(0x35191622) else Color(0xC7FFFFFF))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (darkTheme) 0.12f else 0.55f),
                    shape = CircleShape
                )
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
                
                // Personal branding avatar with high-end premium gradient branding
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (darkTheme) {
                                    listOf(Color(0xFFD0BCFF), Color(0xFF1EBF53))
                                } else {
                                    listOf(Color(0xFF6750A4), Color(0xFF25D366))
                                }
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SV", // Silmar Vargas
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
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
    val darkTheme = isSystemInDarkTheme()

    // Premium Glass Color Specs for light and dark environments
    val glassBgColor = if (isSelected) {
        if (darkTheme) Color(0x664F378B) else Color(0x66EADDFF)
    } else {
        if (darkTheme) Color(0x33100E17) else Color(0xA1FFFFFF)
    }
    
    val glassBorderColor = if (isSelected) {
        if (darkTheme) Color(0xAA9F87DF) else Color(0xAA6750A4)
    } else {
        Color.White.copy(alpha = if (darkTheme) 0.12f else 0.55f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = glassBorderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("contact_item_card_${contact.id}"),
        colors = CardDefaults.cardColors(containerColor = glassBgColor),
        shape = RoundedCornerShape(22.dp)
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
                // High-End avatar bubble with gradient details and customized light stroke
                Box(
                    modifier = Modifier
                        .size(50.dp) // Perfect proportions
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colorPair.first, colorPair.first.copy(alpha = 0.6f))
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = colorPair.second.copy(alpha = 0.25f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (contact.bank.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (darkTheme) Color(0x3D1EBF53) else Color(0x2425D366)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = (if (darkTheme) Color(0xFF1EBF53) else Color(0xFF128C7E)).copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = contact.bank,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (darkTheme) Color(0xFFEADDFF) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (contact.pixKey.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (darkTheme) Color(0x2B1D1B20) else Color(0xCCDCF8C6)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF075E54).copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Pix: ${contact.pixKey}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (darkTheme) Color(0xFF81C784) else Color(0xFF075E54),
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

            // Quick send WhatsApp trigger - polished glowing emerald/green gradient bubble
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF25D366), Color(0xFF128C7E))
                        )
                    )
                    .clickable(onClick = onQuickWhatsApp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .testTag("quick_whatsapp_${contact.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Zapear Rápido",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
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

    val darkTheme = isSystemInDarkTheme()
    val glassBg = if (darkTheme) Color(0xF2161224) else Color(0xEDF7F5FC)
    val glassLine = Color.White.copy(alpha = if (darkTheme) 0.15f else 0.55f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(glassLine, Color.Transparent)
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .testTag("contact_detail_panel"),
        colors = CardDefaults.cardColors(
            containerColor = glassBg
        ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
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
                    fontWeight = FontWeight.ExtraBold,
                    color = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
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
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Telefone",
                    modifier = Modifier.size(14.dp),
                    tint = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
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
                        tint = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
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

            // Bancos & Pix Visual Cards styled with beautiful glassy outline borders
            if (contact.bank.isNotEmpty() || contact.pixKey.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (contact.bank.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = if (darkTheme) 0.12f else 0.45f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0x33100E17) else Color(0x99FFFFFF)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "BANCO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    color = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
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
                            modifier = Modifier
                                .weight(1.3f)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = if (darkTheme) 0.12f else 0.45f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (darkTheme) Color(0x33100E17) else Color(0x99FFFFFF)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CHAVE PIX",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        color = if (darkTheme) Color(0xFF1EBF53) else Color(0xFF075E54)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Copiar Pix",
                                        tint = if (darkTheme) Color(0xFF1EBF53) else Color(0xFF075E54),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = if (darkTheme) 0.08f else 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) Color(0x1F100E17) else Color(0x66FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                fontWeight = FontWeight.ExtraBold,
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
                    focusedContainerColor = if (darkTheme) Color(0x33100E17) else Color(0x99FFFFFF),
                    unfocusedContainerColor = if (darkTheme) Color(0x33100E17) else Color(0x99FFFFFF)
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
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    )
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

    val darkTheme = isSystemInDarkTheme()
    val glassBg = if (darkTheme) Color(0xF21C1824) else Color(0xF2FCFAFF)
    val glassBorder = Color.White.copy(alpha = if (darkTheme) 0.15f else 0.45f)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(
                    width = 1.dp,
                    color = glassBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("add_edit_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = glassBg
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
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
                    fontWeight = FontWeight.ExtraBold,
                    color = if (darkTheme) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
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

@Composable
fun SettingsDialog(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    appPassword: String,
    onPasswordSave: (String) -> Unit,
    contactsList: List<Contact>,
    viewModel: ContactViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val darkTheme = isSystemInDarkTheme()
    
    val glassBg = if (darkTheme) Color(0xF2161224) else Color(0xEDF7F5FC)
    val glassBorder = Color.White.copy(alpha = if (darkTheme) 0.15f else 0.45f)
    
    val sharedPref = remember { context.getSharedPreferences("zap_agenda_prefs", Context.MODE_PRIVATE) }
    var localBackupTime by remember { mutableStateOf(sharedPref.getString("local_backup_data_time", "") ?: "") }
    
    var tempPassword by remember { mutableStateOf(appPassword) }
    var importText by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(
                    width = 1.dp,
                    color = glassBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("settings_dialog_panel"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = glassBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF25D366)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ajustes Gerais",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 1. Theme and color preset selector
                Text(
                    text = "Aparência e Cores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val themesList = listOf("Royal Amethyst", "Emerald WhatsApp", "Sunset Gold", "Deep Sapphire", "Cyber Silver")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    themesList.forEach { theme ->
                        val themeColor = when(theme) {
                            "Emerald WhatsApp" -> Color(0xFF25D366)
                            "Sunset Gold" -> Color(0xFFFFB74D)
                            "Deep Sapphire" -> Color(0xFF4FC3F7)
                            "Cyber Silver" -> Color(0xFFCFD8DC)
                            else -> Color(0xFF6750A4)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                                .clickable { onThemeChange(theme) }
                                .border(
                                    width = if (currentTheme == theme) 3.dp else 1.dp,
                                    color = if (currentTheme == theme) {
                                        if (darkTheme) Color.White else Color.Black
                                    } else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentTheme == theme) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = if (theme == "Cyber Silver") Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "Tema ativo: $currentTheme",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 2. Notifications Config
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lembretes Locais",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Receba lembretes locais para realizar backups da rotina",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF25D366),
                            checkedTrackColor = Color(0xFF25D366).copy(alpha = 0.4f)
                        )
                    )
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 3. Local offline backup persistence (salvar dados localmente backup)
                Text(
                    text = "Backup Interno Offline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) Color(0x1F100E17) else Color(0x66FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Ponto de Restauração",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (localBackupTime.isNotEmpty()) {
                                "Último backup: $localBackupTime"
                            } else {
                                "Nenhum backup local salvo ainda."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val backupJson = exportToJSON(contactsList)
                                    val now = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                    sharedPref.edit()
                                        .putString("local_backup_data", backupJson)
                                        .putString("local_backup_data_time", now)
                                        .apply()
                                    localBackupTime = now
                                    Toast.makeText(context, "Backup local criado com sucesso!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Criar Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    val backupJson = sharedPref.getString("local_backup_data", "") ?: ""
                                    if (backupJson.isNotEmpty()) {
                                        try {
                                            val count = importFromJSON(backupJson, viewModel)
                                            Toast.makeText(context, "$count contatos restaurados com sucesso!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Falha ao restaurar backup local.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Não há backup para restaurar.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = localBackupTime.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Text("Restaurar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 4. Import / Export Backup with quick copy transfer
                Text(
                    text = "Importar / Exportar Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Arraste de outros aparelhos copiando e colando a representação estruturada de cópia de segurança.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = {
                        val backupStr = exportToJSON(contactsList)
                        clipboardManager.setText(AnnotatedString(backupStr))
                        Toast.makeText(context, "Código de backup copiado para área de transferência!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar (Copiar Código para Área)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Código de Importação JSON") },
                    placeholder = { Text("Cole o objeto JSON aqui...") },
                    maxLines = 4
                )
                
                Button(
                    onClick = {
                        try {
                            val count = importFromJSON(importText, viewModel)
                            if (count > 0) {
                                Toast.makeText(context, "$count contatos importados com sucesso!", Toast.LENGTH_SHORT).show()
                                importText = ""
                            } else {
                                Toast.makeText(context, "Nenhum contato válido encontrado no código.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Formato inválido. Verifique o código e tente de novo.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = importText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("Processar & Restaurar Backup colado", fontWeight = FontWeight.Bold)
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 5. Access control password settings (colocar senha no aplicativo)
                Text(
                    text = "Acesso de Segurança",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Defina uma senha numérica ou string para bloquear sua agenda na entrada do app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    modifier = Modifier.fillMaxWidth().testTag("settings_password_form_input"),
                    label = { Text("Senha do Aplicativo") },
                    placeholder = { Text("Preencha para bloquear ou limpe para remover") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                
                Button(
                    onClick = {
                        onPasswordSave(tempPassword.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salvar Configurações de Senha", fontWeight = FontWeight.Bold)
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                // 6. DEVELOPER COPYRIGHT CREDITS
                Text(
                    text = "Copyright © 2026 ZapAgenda • desenvolvido por khallen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
fun AppLockScreen(
    correctPassword: String,
    onUnlock: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val darkTheme = isSystemInDarkTheme()
    
    val lockGradient = remember(darkTheme) {
        if (darkTheme) {
            Brush.verticalGradient(
                colors = listOf(Color(0xFF130E22), Color(0xFF0B1426), Color(0xFF09121E))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFE8E5F3), Color(0xFFD6E4F6), Color(0xFFD9EADB))
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lockGradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (darkTheme) 0.15f else 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (darkTheme) Color(0x731C182A) else Color(0xD8FFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF25D366), Color(0xFF128C7E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App Bloqueado",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Acesso Restrito",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Digite a senha de segurança para acessar o ZapAgenda.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        if (isError) isError = false
                    },
                    modifier = Modifier.fillMaxWidth().testTag("lock_password_input"),
                    label = { Text("Senha do Aplicativo") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = {
                        if (isError) {
                            Text("Senha incorreta. Tente novamente.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                
                Button(
                    onClick = {
                        if (passwordInput == correctPassword) {
                            onUnlock()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("lock_unlock_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    )
                ) {
                    Text("Desbloquear", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

fun exportToJSON(contacts: List<Contact>): String {
    try {
        val array = org.json.JSONArray()
        contacts.forEach { contact ->
            val obj = org.json.JSONObject()
            obj.put("name", contact.name)
            obj.put("phoneNumber", contact.phoneNumber)
            obj.put("email", contact.email)
            obj.put("bank", contact.bank)
            obj.put("pixKey", contact.pixKey)
            obj.put("notes", contact.notes)
            array.put(obj)
        }
        return array.toString(2)
    } catch (e: Exception) {
        return "[]"
    }
}

fun importFromJSON(jsonStr: String, viewModel: ContactViewModel): Int {
    if (jsonStr.isBlank()) return 0
    var importedCount = 0
    val array = org.json.JSONArray(jsonStr)
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val name = obj.optString("name", "")
        val phone = obj.optString("phoneNumber", "")
        if (name.isNotBlank() && phone.isNotBlank()) {
            val email = obj.optString("email", "")
            val bank = obj.optString("bank", "")
            val pixKey = obj.optString("pixKey", "")
            val notes = obj.optString("notes", "")
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
            importedCount++
        }
    }
    return importedCount
}
