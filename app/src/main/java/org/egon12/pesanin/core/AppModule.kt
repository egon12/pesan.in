package org.egon12.pesanin.core

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.egon12.pesanin.dao.OrderDao
import org.egon12.pesanin.dao.OrderItemDao
import org.egon12.pesanin.dao.ProductDao
import org.egon12.pesanin.repository.OrderRepository
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

    @Provides
    fun provideRepository(
        orderDao: OrderDao,
        productDao: ProductDao,
        orderItemDao: OrderItemDao,
    ): OrderRepository {
        return OrderRepository(orderDao, productDao, orderItemDao)
    }
}