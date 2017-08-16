
Here is a listing of the bash scripts in this folder:

build.sh: 
Assumes that the source code is in a directory src within the user's home directory.  
Compiles to bin directory (within your home directory).  Generates a Manifest.txt for
use in generating jar file, and then generates algengine.jar within the lib directory 
within user's home directory.  The algengine.jar is executable and is used to start up
the RMI server with the parallel algorithms.

After building, copy lib/algengine.jar to all worker nodes.  No other java or class files
are needed on the workers.

rebootAll.sh:
Reboots all worker nodes of your cluster.  Assumes that you have ssh keys configured
to enable ssh without password.  Also assumes that the worker nodes are named rpi1.local
through rpi7.local.  Does not reboot rpi0.  Must be run by a user with sudo access.

shutdownAll.sh:
Like rebootAll except it just shuts down all nodes.  Same assumptions and requirements
as rebootAll.

startAlgEngine.sh:
This script starts the rmi server.  Copy scripts/startAlgEngine.sh to all worker nodes.
You only need it on the master node if you will run an RMI server there as well.

startWorkers.sh:
Assumes that ssh keys have been configured to enable ssh without password.  This script
executes startAlgEngine.sh on all worker nodes (rpi1 through rpi7).

