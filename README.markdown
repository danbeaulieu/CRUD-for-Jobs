CRUD for Jobs for Play! Framework v0.1
======================================
The CRUD (Create, Read, Update, Delete) for Jobs module is a module that adds a Job info to your existing 
CRUD admin. Useful for quickly seeing which jobs have run, last time they ran, how they are scheduled to run, etc.

In order to use this module you'll need to add it do your applications dependencies.yaml
and run:
> play dependencies {appName}

your application.conf will also need a db enabled as well. No persistent data yet, so you can get away with 

> db=mem


> **NOTE** crudjobs v0.1 depends on the crud module, be sure the module is configured correctly (routes are added, dependency is installed, etc)

