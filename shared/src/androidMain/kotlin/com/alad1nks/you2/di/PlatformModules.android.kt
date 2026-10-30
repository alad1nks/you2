package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.FirebaseAuthClient
import com.alad1nks.you2.core.data.repository.FirestoreWishesRepository
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.storage.datastore.di.StorageDataStoreModule
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose
import kotlin.time.Clock

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageDataStoreModule,
        module {
            single<AuthClient> { FirebaseAuthClient() }
            single<DateSource> {
                DateSource {
                    Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault(),
                    ).date
                }
            }
            single<WishesRepository> {
                FirestoreWishesRepository(
                    get(),
                )
            } onClose { (it as? FirestoreWishesRepository)?.close() }
        },
    )
