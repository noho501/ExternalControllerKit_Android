package com.noho501.externalcontrollerkit.di

import com.noho501.externalcontrollerkit.logger.ConsoleLogger
import com.noho501.externalcontrollerkit.logger.Logger
import com.noho501.externalcontrollerkit.storage.DataStoreMappingStorage
import com.noho501.externalcontrollerkit.storage.MappingStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExternalControllerModule {

    @Binds
    @Singleton
    abstract fun bindLogger(logger: ConsoleLogger): Logger

    @Binds
    @Singleton
    abstract fun bindMappingStorage(storage: DataStoreMappingStorage): MappingStorage
}
