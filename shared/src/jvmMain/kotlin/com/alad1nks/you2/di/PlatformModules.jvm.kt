package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.DesktopAuthClient
import com.alad1nks.you2.auth.DesktopSessionStore
import com.alad1nks.you2.cloud.DesktopCloudProvider
import com.alad1nks.you2.cloud.NativeCloudRepository
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
    )

internal fun desktopPlatformModules(sessionStore: DesktopSessionStore): List<Module> {
    val authModule =
        module {
            val definition = single<AuthClient> { DesktopAuthClient(store = sessionStore) }
            definition onClose { (it as? DesktopAuthClient)?.close() }
            single<DateSource> {
                DateSource {
                    Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault(),
                    ).date
                }
            }
            single {
                val auth = get<AuthClient>() as DesktopAuthClient
                DesktopCloudProvider(auth, auth::tokenFor)
            } onClose { it?.close() }
            single<WishesRepository> {
                NativeCloudRepository(get<DesktopCloudProvider>(), get(), get())
            } onClose { (it as? NativeCloudRepository)?.close() }
        }
    return PlatformModules + authModule
}
