package com.example.accessingvault

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.vault.core.*
import org.springframework.vault.support.VaultMount

@SpringBootApplication
class AccessingVaultApplication {

    @Bean
    fun run(vaultTemplate: VaultTemplate): CommandLineRunner {
        return CommandLineRunner {
            /**
             * You usually would not print a secret to stdout
             */
            val response = vaultTemplate
                .opsForKeyValue("secret", VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get("github")
            println("Value of github.oauth2.key")
            println("---------------------------------")
            println(response!!.data!!["github.oauth2.key"])
            println("---------------------------------")
            println()


            /**
             * Let's encrypt some data using the Transit backend.
             */
            val transitOperations = vaultTemplate.opsForTransit()

            /**
             * We need to set up transit first (assuming you didn't set up it yet)
             */

            val  sysOperations = vaultTemplate.opsForSys()

            if (!sysOperations.mounts.containsKey("transit/")) {
                sysOperations.mount("transit", VaultMount.create("transit"))
                transitOperations.createKey("foo-key")
            }

            /**
             * Encrypt a plain-text value
             */
            val ciphertext = transitOperations.encrypt("foo-key", "Secure message")

            println("Encrypted value")
            println("---------------------------------")
            println(ciphertext)
            println("---------------------------------")
            println()

            /**
             * Decrypt
             */
            val plaintext = transitOperations.decrypt("foo-key", ciphertext)
            println("Decrypted value")
            println("---------------------------------")
            println(plaintext)
            println("---------------------------------")
            println()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<AccessingVaultApplication>(*args)
}
