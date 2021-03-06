package notifier;

import core.NotificationLimit;
import core.NovaBot;
import net.dv8tion.jda.core.entities.User;

class NotificationSender {

    public final String WHITE_GREEN_CHECK = "\u2705";

    NovaBot novaBot;

    boolean checkSupporterStatus(User user) {
        NotificationLimit limit = novaBot.config.getNotificationLimit(novaBot.guild.getMember(user));

        boolean passedChecks = true;

        int pokeCount = novaBot.dbManager.countPokemon(user.getId(), novaBot.config.countLocationsInLimits());
        if (limit.pokemonLimit != null && pokeCount > limit.pokemonLimit) {
            resetUser(user,limit);
            passedChecks = false;
        }

        if (passedChecks) {
            int presetCount = novaBot.dbManager.countPresets(user.getId(), novaBot.config.countLocationsInLimits());
            if (limit.presetLimit != null && presetCount > limit.presetLimit) {
                resetUser(user,limit);
                passedChecks = false;

            }

            if (passedChecks) {
                int raidCount = novaBot.dbManager.countRaids(user.getId(), novaBot.config.countLocationsInLimits());
                if (limit.raidLimit != null && raidCount > limit.raidLimit) {
                    resetUser(user,limit);
                    passedChecks = false;
                }
            }
        }
        return passedChecks;
    }

    private void resetUser(User user, NotificationLimit newLimit) {
        novaBot.dbManager.resetUser(user.getId());

        user.openPrivateChannel().queue(channel -> channel.sendMessageFormat("Hi %s, I noticed that recently your supporter status has changed." +
                " As a result I have cleared your settings. At your current level you can add up to %s to your settings.",user,newLimit.toWords()).queue());

        if (novaBot.config.loggingEnabled()) {
            novaBot.roleLog.sendMessageFormat("%s's supporter status has changed, requiring a reset of their settings. They have been informed via PM.", user).queue();
        }
    }
}
