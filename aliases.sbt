addCommandAlias("cc", "clean;compile")
addCommandAlias("all", "reload;update;clean;compile")
addCommandAlias("ce", "reload;update;clean;compile;Test / clean;Test / compile")
addCommandAlias("servert", "project server;test;project app")
addCommandAlias("cib", "deleteBloop;bloopInstall")
addCommandAlias("sfix", "scalafixEnable;scalafixAll --rules OrganizeImports")
addCommandAlias("itt", "IntegrationTest / test")
addCommandAlias("runt",
                "project server;test;project app;IntegrationTest / test")
