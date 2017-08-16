/*
 * Copyright 2017 Vincent A. Cicirello.
 *
 * This file is part of package org.cicirello.algengine.
 *
 * Java package org.cicirello.algengine is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * Java package org.cicirello.algengine is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java package org.cicirello.permutations.  If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */
package org.cicirello.algengine;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cicirello.math.Pi;
import org.cicirello.matrixops.ConcurrentMatrixMultiplier;

/**
 * A Java RMI server to execute parts of parallel algorithms remotely.
 * 
 * @author Vincent A. Cicirello
 * @version 8.15.2017
 */
public final class ParallelAlgorithmEngine implements RemoteParallelAlgorithms {

	private final ConcurrentMatrixMultiplier mult;
	private final ExecutorService es;
	
	private ParallelAlgorithmEngine(int warmLength) {
		es = Executors.newCachedThreadPool();
		mult = new ConcurrentMatrixMultiplier(1, es);
		try { //warmup
			// This forces the Java JIT compiler to compile the hot spots of the
			// methods natively, rather than waiting for the first incoming
			// RMI call.
			multiply(4, new double[warmLength][warmLength], new double[warmLength][warmLength]);
			Pi.concurrentPi(100, 4, es);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * {@inheritDoc}
	  */
	@Override
	public double pi(int n, int numThreads) throws RemoteException {
		return Pi.concurrentPi(n, numThreads, es);
	}
	
	/**
	  * {@inheritDoc}
	  */
	@Override
	public final double[][] multiply(int numThreads, double[][] a, double[][] bTranspose) throws RemoteException {
		mult.setThreads(numThreads);
		return mult.multiply(a, bTranspose, true);
	}

	/**
	 * Starts up the RMI parallel algorithm server.
	 * @param args Command line arguments.  Currently none.
	 */
	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	int warmLength = 128;
        	if (args.length > 0) {
        		warmLength = Integer.parseInt(args[0]);
        	}
            String name = "Alg";
            final RemoteParallelAlgorithms engine = new ParallelAlgorithmEngine(warmLength);
            RemoteParallelAlgorithms stub =
                (RemoteParallelAlgorithms) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Parallel Algorithm Server Initiated");
        } catch (RemoteException e) {
            System.err.println("Exception occurred during parallel algorithm server initialization:");
            e.printStackTrace();
        }
	}

	

}
