package com.example.data

import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    fun getAllContacts(): Flow<List<Contact>> = contactDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<Contact>> = contactDao.searchContacts(query)

    suspend fun insert(contact: Contact): Long = contactDao.insertContact(contact)

    suspend fun update(contact: Contact) = contactDao.updateContact(contact)

    suspend fun delete(contact: Contact) = contactDao.deleteContact(contact)
}
