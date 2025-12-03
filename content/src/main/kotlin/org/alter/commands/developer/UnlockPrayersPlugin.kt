package org.alter.commands.developer

import org.alter.api.ClientScript
import org.alter.api.ext.*
import org.alter.game.model.LockState
import org.alter.game.model.move.moveTo
import org.alter.game.model.priv.Privilege
import org.alter.game.pluginnew.PluginEvent
import org.alter.game.pluginnew.event.impl.CommandEvent
import org.alter.rscm.RSCM
import org.alter.rscm.RSCM.asRSCM
import org.alter.api.ext.message
import org.alter.api.ext.prepareForTeleport

class UnlockPrayersPlugin : PluginEvent() {

    companion object {
        private const val RIGOUR_UNLOCK_VARBIT = "varbits.prayer_rigour_unlocked"
        private const val AUGURY_UNLOCK_VARBIT = "varbits.prayer_augury_unlocked"
        private const val PRESERVE_UNLOCK_VARBIT = "varbits.prayer_preserve_unlocked"
        private const val HUMBLE_CHIVALRY_VARBIT = "varbits.humble_chivalry"
        private const val HUMBLE_PIETY_VARBIT = "varbits.humble_piety"
        private const val KING_RANSOMS_QUEST_VARBIT = "varbits.kr_knightwaves_state"
    }

    override fun init() {
        on<CommandEvent> {
            where {
                command.equals("unlockprayers", ignoreCase = true) &&
                player.world.privileges.isEligible(player.privilege, Privilege.DEV_POWER)
            }
            then {

            // Toggle each prayer unlock varbit
            val rigourUnlocked = player.getVarbit(RIGOUR_UNLOCK_VARBIT) == 1
            val auguryUnlocked = player.getVarbit(AUGURY_UNLOCK_VARBIT) == 1
            val preserveUnlocked = player.getVarbit(PRESERVE_UNLOCK_VARBIT) == 1
            val humbleChivalryUnlocked = player.getVarbit(HUMBLE_CHIVALRY_VARBIT) == 1
            val humblePietyUnlocked = player.getVarbit(HUMBLE_PIETY_VARBIT) == 1
            val krState = player.getVarbit(KING_RANSOMS_QUEST_VARBIT)

            // Toggle all to opposite state
            player.setVarbit(RIGOUR_UNLOCK_VARBIT, if (rigourUnlocked) 0 else 1)
            player.setVarbit(AUGURY_UNLOCK_VARBIT, if (auguryUnlocked) 0 else 1)
            player.setVarbit(PRESERVE_UNLOCK_VARBIT, if (preserveUnlocked) 0 else 1)
            player.setVarbit(HUMBLE_CHIVALRY_VARBIT, if (humbleChivalryUnlocked) 0 else 1)
            player.setVarbit(HUMBLE_PIETY_VARBIT, if (humblePietyUnlocked) 0 else 1)
            player.setVarbit(KING_RANSOMS_QUEST_VARBIT, if (krState >= 8) 0 else 8) // Unlocks Chivalry and Piety

                // Sync prayer unlocks - we need to run the client script and sync varbits
                // Since we can't import Prayers from game-plugins, we'll do it manually
                player.setVarbit(RIGOUR_UNLOCK_VARBIT, player.getVarbit(RIGOUR_UNLOCK_VARBIT))
                player.setVarbit(AUGURY_UNLOCK_VARBIT, player.getVarbit(AUGURY_UNLOCK_VARBIT))
                player.setVarbit(PRESERVE_UNLOCK_VARBIT, player.getVarbit(PRESERVE_UNLOCK_VARBIT))
                player.setVarbit(HUMBLE_CHIVALRY_VARBIT, player.getVarbit(HUMBLE_CHIVALRY_VARBIT))
                player.setVarbit(HUMBLE_PIETY_VARBIT, player.getVarbit(HUMBLE_PIETY_VARBIT))
                player.runClientScript(ClientScript(id = 2158))

                val action = if (rigourUnlocked) "Locked" else "Unlocked"
                player.message("$action all prayers: Rigour, Augury, Preserve, Chivalry, and Piety")
            }
        }

        //Teleport
        on<CommandEvent> {
            where {
                command.startsWith("Teleport", ignoreCase = true) || command.startsWith("TP", ignoreCase = true)
            }
            then {
                val Args = arguments ?: emptyArray()

                if (Args.isEmpty()) {
                    player.message("Provide X and Y, and optionally Z")
                    return@then
                }

                val X = Args.getOrNull(0)?.toIntOrNull()
                if (X == null) {
                    player.message("Must Provide X Coordinate")
                    return@then
                }
                val Y = Args.getOrNull(1)?.toIntOrNull()
                if (Y == null) {
                    player.message("Must Provide Y Coordinate")
                    return@then
                }
                val Z = Args.getOrNull(2)?.toIntOrNull() ?: 0

                player.prepareForTeleport()
                player.lock = LockState.FULL_WITH_DAMAGE_IMMUNITY

                player.animate("sequences.poh_smash_magic_tablet", delay = 16)
                player.playSound("jingles.artistry".asRSCM(), volume = 1, delay = 15)

                player.animate(RSCM.NONE)
                player.unlock()

                player.moveTo(X,Y,Z)
                player.message("You Teleport to $X ,$Y ,$Z")
            }
        }

        //AddItem
        on<CommandEvent> {
            where {
                command.startsWith("AddItem", ignoreCase = true) || command.startsWith("Give", ignoreCase = true)
            }
            then {
                val Args = arguments ?: emptyArray()

                if (Args.isEmpty()) {
                    player.message("Provide ItemID, and optionally, Amount.")
                    return@then
                }

                val itemId = Args.getOrNull(0)?.toIntOrNull()
                if (itemId == null) {
                    player.message("Item ID must be a number. You typed: ${Args.getOrNull(0)}")
                    return@then
                }

                val amount = Args.getOrNull(1)?.toIntOrNull() ?: 1

                player.inventory.add(itemId, amount, assureFullInsertion = false)
                player.message("You add $amount of item $itemId to your inventory.")
            }
        }
    }
}