package com.alad1nks.startkmp.di

import com.alad1nks.startkmp.core.data.di.DataModule
import com.alad1nks.startkmp.core.domain.di.DomainModule
import com.alad1nks.startkmp.core.storage.common.di.StorageCommonModule
import org.koin.core.module.Module

val CommonModules: List<Module> get() =
    listOf(
        DataModule,
        DomainModule,
        SharedModule,
        StorageCommonModule,
    )
