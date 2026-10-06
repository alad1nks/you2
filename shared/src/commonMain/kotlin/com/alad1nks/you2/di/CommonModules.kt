package com.alad1nks.you2.di

import com.alad1nks.you2.core.data.di.DataModule
import com.alad1nks.you2.core.domain.di.DomainModule
import com.alad1nks.you2.core.storage.common.di.StorageCommonModule
import org.koin.core.module.Module

val CommonModules: List<Module> get() =
    listOf(
        DataModule,
        DomainModule,
        SharedModule,
        StorageCommonModule,
    )
