addCommandAlias("cc", "clean;compile")
addCommandAlias("all", "reload;update;clean;compile")
addCommandAlias("servert", "project server;test;project app")
addCommandAlias("cib", "deleteBloop;bloopInstall")
addCommandAlias("sfix", "scalafixEnable;scalafixAll --rules OrganizeImports")
addCommandAlias("itt", "it:test")