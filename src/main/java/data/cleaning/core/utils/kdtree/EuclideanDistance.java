// EuclideanDistance.java : Class for Euclidean distance metric
//
// Copyright (C) Simon D. Levy 2014
//
// This code is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as 
// published by the Free Software Foundation, either version 3 of the 
// License, or (at your option) any later version.
//
// This code is distributed in the hope that it will be useful,     
// but WITHOUT ANY WARRANTY without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License 
//  along with this code.  If not, see <http://www.gnu.org/licenses/>.
//  You should also have received a copy of the Parrot Parrot AR.Drone 
//  Development License and Parrot AR.Drone copyright notice and disclaimer 
//  and If not, see 
//   <https://projects.ardrone.org/attachments/277/ParrotLicense.txt> 
// and
//   <https://projects.ardrone.org/attachments/278/ParrotCopyrightAndDisclaimer.txt>.

package data.cleaning.core.utils.kdtree;

public class EuclideanDistance implements DistanceMetric {

	@Override
	public float distance(float[] a, float[] b) {

		return (float) Math.sqrt(sqrdist(a, b));

	}

	public static float sqrdist(float[] a, float[] b) {

		float dist = 0;

		for (int i = 0; i < a.length; ++i) {
			float diff = (a[i] - b[i]);
			dist += diff * diff;
		}

		return dist;
	}
}
