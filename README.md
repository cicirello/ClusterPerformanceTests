# ClusterPerformanceTests
# Performance Tests for Small Clusters

Copyright (C) 2017 Vincent A. Cicirello.

https://www.cicirello.org/

The code provided in this repository was developed to test the performance of a small 8 node cluster of raspberry pis.  Implementations of a few simple parallel algorithms for testing the performance of a small cluster.  Uses Java RMI for communication between master node and the worker nodes.  See the Java docs, in the doc folder, for other implementation details.  The scripts folder contains useful bash scripts such as for starting the RMI servers on the worker nodes, shutting down and rebooting worker nodes from the master node, etc.  See the scripts/README.txt file for details of the usage of the available scripts.
