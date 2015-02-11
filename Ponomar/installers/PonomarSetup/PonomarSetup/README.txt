README for the PonomarSetup Visual Studio project.

Author:  Matthew J. Fisher
Created: 2015-02-10

This project was created using the community edition of Microsoft Visual Studio
2013, and the (open source) WiX Toolset (version 3.96).

When built, this project creates an MSI installer and two .cab files.

When run, the MSI installer creates the Ponomar directory under the applicable
Program Files directory, which means either either "Program Files" or "Program
Files (x86)". It also creates an application shortcut in the applicable Start
menu, which differs depending on the Windows version.

The contents of the Ponomar application directory are as defined in the
Components.wxs file. The Components.wxs file itself was generated as follows:

- Build Ponomar.jar in the NetBeans IDE.
- Use launch4j to create Ponomar.exe, which wraps the .jar file.
- Create a folder named SourceFiles on the Desktop (or wherever).
- Copy the following directories from the Ponomar source tree to that folder:
    images
	languages
	xml
- Create a subfolder named bin, and place Ponomar.exe in it.
- Create a subfolder named src, and place our .java source code files in it.
- The SourceFiles folder thus contains the following subdirectories:
    bin
	images
	languages
	src
	xml
- In the PonomarSetup project's Properties, on the Build tab, define the
  following preprocessor variable (revise the path as applicable):
    SourceFilesDir=C:\Users\Matthew\Desktop\SourceFiles
- In a Windows command prompt, cd to the directory containing SourceFiles.
  Then execute the following (revising the WIX value as needed):
    set WIX=C:\Program Files (x86)\WiX Toolset v3.9\bin
    "%WIX%\bin\heat.exe" dir "SourceFiles" -cg MyComponentGroup ^
	  -dr INSTALLFOLDER -gg -sfrag -srd -var var.SourceFilesDir ^
	  -out "Components.wxs"

I then added the resulting Components.wxs file to the PonomarSetup project.

The procedure above was based on some quick reading in Nick Ramirez's
book "Wix Cookbook" (Packt Publishing, 2015).

So that allows us to build two .cab files and a .msi file.

It's convenient to wrap these three files in a self-extracting installer.
Microsoft's iexpress utility is good for that purpose.
The .SED file in this project is for use with iexpress.

As desired, we could add a post-build step to run iexpress.
For now, I just run it manually, and load the .SED file when prompted.

TODO: Create the .jar file in Eclipse instead, and then recreate Ponomar.exe.
      When created in NetBeans, there's something wrong with the appearance of
	  the next/previous icon buttons on the application's main form.

TODO: Test in Windows 7, to see whether the MSI installer works there.

TODO: Ideally we should assist the user with meeting prerequisites, which in
	  this case means assisting them with installing a suitable JRE as needed.
	  That might be best done in the launch4j configuration.

TODO: Add license info to the files in this project (or just a LICENSE file).

TODO: We need to include one or more of the following files in the application
	  folder: README, LICENSE, COPYING. Credit should be given as needed for
	  3rd party content and code.

TODO: When ready to upload, we'll need web hosting for the binary downloads.
