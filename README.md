CraftBook
=========

CraftBook adds mechanisms (bridges, gates), redstone additions (integrated circuits, programmable logic chips), and minecart enhancements to Minecraft SMP (multiplayer). It does this without requiring on client-side modifications by using heuristics based on block and sign  placement instead. It is currently a plugin for hMod (which is now being ported to Bukkit), although CraftBook will be ported over to the Minecraft API once it becomes available and sufficient.

About the Project
-----------------

We have an IRC channel on Espernet: [#craftbook on irc.esper.net](irc://irc.esper.net/craftbook). You'll find fellow CraftBook users and developers there.

* [https://github.com/sk89q/craftbook](https://github.com/sk89q/craftbook) - source code and the place to report issues
* [http://www.redmine.sk89q.com/projects/craftbook](http://www.redmine.sk89q.com/projects/craftbook) -  main project issue tracker, where we plan and plot milestones (it's superior to GitHub's issue tracker)
* [http://wiki.sk89q.com/wiki/CraftBook](http://wiki.sk89q.com/wiki/CraftBook) - wiki-based documentation

Contributing
------------

We happily accept contributions. The best way to do this is to fork CraftBook on GitHub, add your changes, and then submit a pull request. We'll look at it, make comments, and merge it into CraftBook if everything works out. You can fix bugs, add features, write new ICs -- it's up to you. Check the redmine.sk89q.com site for our "to do" list.

Make sure to keep these points in mind when submitting code:

* Ensure that the feature doesn't consume too much CPU. Some features, like ICs, could possibly run every two ticks.
* Keep memory usage to a minimum.
* Consider network bandwidth usage. For example, updating signs requires a large packet to be sent.

Don't worry though; we'll make suggestions if we find any issue with those points.

Your submissions have to be licensed under the GNU General Public License v3.