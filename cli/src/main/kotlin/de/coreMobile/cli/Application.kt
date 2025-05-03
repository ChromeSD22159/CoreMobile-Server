package de.coreMobile.cli

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import de.coreMobile.cli.commands.InitCommand
import de.coreMobile.cli.commands.RunCli
import de.coreMobile.cli.commands.RunCommand
import de.coreMobile.cli.commands.StopCommand

fun main(args: Array<String>){
    RunCli()
        .subcommands(InitCommand(), RunCommand(), StopCommand())
        .main(args)
}