package com.twomax.live.data.di

import android.content.Context
import androidx.room.Room
import com.twomax.live.data.local.db.TwoMaxDatabase
import com.twomax.live.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TwoMaxDatabase {
        return Room.databaseBuilder(
            context,
            TwoMaxDatabase::class.java,
            "twomax_database"
        )
            .addMigrations(TwoMaxDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideProviderDao(db: TwoMaxDatabase): ProviderDao = db.providerDao()
    @Provides fun provideChannelDao(db: TwoMaxDatabase): ChannelDao = db.channelDao()
    @Provides fun provideMovieDao(db: TwoMaxDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: TwoMaxDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: TwoMaxDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideEpgProgramDao(db: TwoMaxDatabase): EpgProgramDao = db.epgProgramDao()
    @Provides fun provideCategoryDao(db: TwoMaxDatabase): CategoryDao = db.categoryDao()
    @Provides fun providePublicEpgSourceDao(db: TwoMaxDatabase): PublicEpgSourceDao = db.publicEpgSourceDao()
}
