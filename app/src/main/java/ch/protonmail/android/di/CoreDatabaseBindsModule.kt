/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.protonmail.android.di

import ch.protonmail.android.data.AppDatabase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.mailsettings.data.db.MailSettingsDatabase
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.usersettings.data.db.UserSettingsDatabase

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CoreDatabaseBindsModule {

    @Binds
    abstract fun provideAccountDatabase(db: AppDatabase): AccountDatabase

    @Binds
    abstract fun provideUserDatabase(db: AppDatabase): UserDatabase

    @Binds
    abstract fun provideAddressDatabase(db: AppDatabase): AddressDatabase

    @Binds
    abstract fun provideKeySaltDatabase(db: AppDatabase): KeySaltDatabase

    @Binds
    abstract fun providePublicAddressDatabase(db: AppDatabase): PublicAddressDatabase

    @Binds
    abstract fun provideHumanVerificationDatabase(db: AppDatabase): HumanVerificationDatabase

    @Binds
    abstract fun provideMailSettingsDatabase(db: AppDatabase): MailSettingsDatabase

    @Binds
    abstract fun provideUserSettingsDatabase(db: AccountManagerDatabase): UserSettingsDatabase
}
