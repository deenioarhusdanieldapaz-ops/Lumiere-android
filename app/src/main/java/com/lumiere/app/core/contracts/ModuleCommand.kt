package com.lumiere.app.core.contracts

/**
 * Contrato mínimo de comando do Core para os módulos.
 * Um comando descreve uma intenção de leitura/ação. O módulo é dono da execução.
 * O Core nunca acede às tabelas do módulo diretamente.
 */
sealed class ModuleCommand {
    data object GetActiveTasksCount : ModuleCommand()
}

sealed class ModuleCommandResult {
    data class Success(val data: Any) : ModuleCommandResult()
    data class Failure(val reason: String) : ModuleCommandResult()
}
