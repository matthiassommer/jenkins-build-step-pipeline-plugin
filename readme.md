Build step and pipeline compatible Jenkins plugin
======


## Build plugin
The plugin is based on a Maven build.
Select the `Jenkins` target and build the project.
A target folder is generated. The plugin is packaged as hpi file, e.g. target/build-project.hpi


## Install plugin in Jenkins
To install the Jenkins Plugin, go to `Jenkins > Manage Jenkins > Manage Plugins > Advanced`.
The direct link would be something like `https://<your-jenkins>/pluginManager/advanced`.
Scroll down to *Upload Plugin*. Select the plugin .hpi file and click *Upload*.

A restart of Jenkins might be needed. 

Afterwards, the plugin is listed under the *Installed* section at `https://<your-jenkins>/pluginManager/installed`.

# Support
If this saved you some time, you may want to [support me](https://www.paypal.me/SommerMatthias/5).
