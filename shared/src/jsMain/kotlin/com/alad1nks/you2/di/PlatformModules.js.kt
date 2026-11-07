package com.alad1nks.you2.di

import com.alad1nks.you2.auth.AuthClient
import com.alad1nks.you2.auth.WebAuthClient
import com.alad1nks.you2.cloud.NativeCloudRepository
import com.alad1nks.you2.cloud.WebCloudProvider
import com.alad1nks.you2.core.data.repository.WishesRepository
import com.alad1nks.you2.core.model.DateSource
import com.alad1nks.you2.core.storage.js.di.StorageJsModule
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose
import kotlin.time.Clock

actual val PlatformModules: List<Module> get() =
    listOf(
        StorageJsModule,
        module {
            single<AuthClient> { WebAuthClient() }
            single<DateSource> {
                DateSource {
                    Clock.System.now().toLocalDateTime(
                        TimeZone.currentSystemDefault(),
                    ).date
                }
            }
            single<WishesRepository> {
                NativeCloudRepository(WebCloudProvider(get()), get(), get())
            } onClose { (it as? NativeCloudRepository)?.close() }
        },
    )
