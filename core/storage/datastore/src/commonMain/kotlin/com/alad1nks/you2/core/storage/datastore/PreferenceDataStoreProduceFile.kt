package com.alad1nks.you2.core.storage.datastore

import okio.Path
import org.koin.core.scope.Scope

expect val Scope.PreferenceDataStoreProduceFile: Path
