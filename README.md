# Java Swing application for managing passwords
In order to save all my passwords without using a third-party software, I develop this application.
A master password encrypt the file with AES256.

The following data can be stored for each website
- Name
- User
- Password
- Hint
- Renew (to describe when it needs to be renewed)
- Deprecated?

An option to generate a PDF file is available. The file will contains only Name, User and Hint values.

Running MyPasswordManagerLauncher.jar is enough to run the program. By using this jar, the program will be updated automatically.