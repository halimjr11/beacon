package com.halimjr11.beacon.domain

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/** Picks up every @Factory use case under com.halimjr11.beacon.domain. */
@Module
@ComponentScan
class DomainModule
