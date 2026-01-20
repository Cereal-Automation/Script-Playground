package com.cereal.script.playground

import com.cereal.licensechecker.LicenseChecker
import com.cereal.licensechecker.LicenseState
import com.cereal.sdk.component.ComponentProvider
import com.cereal.sdk.ExecutionResult
import com.cereal.sdk.Script


class SampleScript : Script<PlaygroundConfiguration> {

    override suspend fun onStart(configuration: PlaygroundConfiguration, provider: ComponentProvider): Boolean {
        return true
    }

    override suspend fun execute(configuration: PlaygroundConfiguration, provider: ComponentProvider, statusUpdate: suspend (message: String) -> Unit): ExecutionResult {
        statusUpdate("Start printing configuration...")

        provider.logger().info("Found boolean config value: ${configuration.booleanValue()}")
        provider.logger().info("Found integer config value: ${configuration.integerValue()}")
        provider.logger().info("Found float config value: ${configuration.floatValue()}")
        provider.logger().info("Found string config value: ${configuration.nullableStringValue()}")

        return ExecutionResult.Success("Printed configuration")
    }

    override suspend fun onFinish(configuration: PlaygroundConfiguration, provider: ComponentProvider) {
    }
}
