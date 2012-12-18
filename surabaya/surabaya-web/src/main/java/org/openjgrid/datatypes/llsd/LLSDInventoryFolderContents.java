/**
 *  Surabaya - a replacement http server for the OpenSimulator
 *  Copyright (C) 2012 Akira Sonoda
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openjgrid.datatypes.llsd;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Akira Sonoda
 *
 */
public class LLSDInventoryFolderContents {
	
    public UUID agent_id; 
    public int descendents;
    public UUID folder_id; 
    public ArrayList<Object> categories = new ArrayList<Object>();
    public ArrayList<Object> items = new ArrayList<Object>();
    public UUID owner_id; 
    public int version;


}
