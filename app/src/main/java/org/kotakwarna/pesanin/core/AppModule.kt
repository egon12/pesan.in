package org.kotakwarna.pesanin.core

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.kotakwarna.pesanin.dao.OrderDao
import org.kotakwarna.pesanin.dao.OrderItemDao
import org.kotakwarna.pesanin.dao.ProductDao
import org.kotakwarna.pesanin.repository.OrderRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "order_management.db"
        ).build()
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao = database.orderDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideOrderItemDao(database: AppDatabase): OrderItemDao = database.orderItemDao()
}