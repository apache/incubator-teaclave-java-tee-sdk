This project provides the necessary configurations and class initializations settings to build applications with BouncyCastle libraries into native image.<br />
This project has two functionalities: Function1, generating the native-image configuration files from BouncyCastle source; and Function2, at native image build time programmatically registering the configurations obtained from Function1.
# Function 1:  Generating Configurations
The basic idea of this function is to run the tests of BouncyCastle with native-image-agent, so that all the dynamic features such as reflection, serialization, dynamic proxy will be recorded and write to the configuration files for native image building. 
BouncyCastle employs Gradle as its build tool, so our work is to modify its build.gradle file. <br />The shell script file`generate_bc_configs.sh` takes two input arguments, BouncyCastle's source code directory path and the BouncyCastle's version. I
It automatically applies the patches, runs tests, and collects configurations.<br />
The `bc-java` directory stores the patches for different BouncyCastle versions.<br />
The output of this function has been already provided in `src/main/resources/configs`. 
It is not necessary to run this function for users. Advanced developers can extend the scripts in this part to add configurations for new versions of BouncyCastle that are not yet included.
# Function 2: BouncyCastleFeature
This feature tells the native-image where to load BouncyCastle's configurations and specifies which classes can be initialized at build time and which classes should be delayed to initialize at run time, so that the application depends on BouncyCastle library can be successfully built into native image.<br />
Simply run `mvn package`, you will get the `target/bouncycastle-native-0.1.0.jar`. Append the bouncycastle-native-0.1.0.jar file to your `native-image` command's `-cp` option. The rest will be automatically done.
