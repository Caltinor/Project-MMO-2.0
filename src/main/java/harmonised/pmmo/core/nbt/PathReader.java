package harmonised.pmmo.core.nbt;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.StringReader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

public class PathReader {
	/**This method takes a raw NBT path (as syntactically defined by
	 * this class's reader, and traces the provided NBT Compound for
	 * the value at that destination.  All values are returned as
	 * Strings.  if a path includes an include-all list, multiple
	 * values will be returned.
	 * 
	 * @param path the NBT path to locate values
	 * @param nbt the source to be searched
	 * @return all possible values at path destinations
	 */	
	public static List<String> getNBTValues(String path, CompoundTag nbt) {
		List<String> nodes = parsePath(path);
		if (nbt.isEmpty() || nbt == null) return new ArrayList<String>();
		return evaluateCompound(nodes, nbt);
	}
	
	private static List<String> parsePath(String path) {
		List<String> nodes = new ArrayList<>();
		StringReader reader = new StringReader(path);
		String element = "";
		while (reader.canRead()) {
			if (reader.peek() == '.') {
				nodes.add(element);
				element = "";
				reader.read();
				continue;
			}
			element += reader.read();
		}
		nodes.add(element);
		return nodes;
	}
	
	private static List<String> evaluateCompound(List<String> nodes, CompoundTag nbt) {
		List<String> list = new ArrayList<>();
		if (nbt.isEmpty() || nbt == null) return list;
		String nodeEntry = nodes.get(0);

		if (isQualifiedNode(nodeEntry) && !isQualifiedCompound(nodeEntry, nbt))
			return list;
		
		if (isList(nodeEntry)) {	
			nodes.remove(0);
			list.addAll(evaluateList(nodes, nodeEntry, (ListTag) nbt.get(rawNode(nodeEntry))));
		}
		else if (isCompound(nodeEntry)) {
			nodes.remove(0);
			list.addAll(evaluateCompound(nodes, nbt.getCompound(rawNode(nodeEntry))));
		}
		else {
			Tag value = nbt.get(rawNode(nodeEntry));
			if (value != null)
				list.add(value.getAsString());
		}
		
		return list;
	}
	
	private static List<String> evaluateList(List<String> nodes, String node, ListTag lnbt) {		
		List<String> list = new ArrayList<>();
		if (lnbt == null) return list;
		int index = getListIndex(node);
		if (index == -2) index = getQualifiedIndex(node, lnbt);
		if (index < -1 || index >= lnbt.size()) return list;
		if (index == -1) {
			for (int l = 0; l < lnbt.size(); l++) {
				if (lnbt.get(0) instanceof CompoundTag) {	
					list.addAll(evaluateCompound(new ArrayList<>(nodes), lnbt.getCompound(l)));
				}
				else if (lnbt.get(0) instanceof ListTag) {
					list.addAll(evaluateList(new ArrayList<>(nodes), getListParameters(nodes.get(0)), lnbt.getList(l)));
				}
				else list.add(lnbt.get(l).getAsString());
			}
		}
		else {
			if (lnbt.get(0) instanceof CompoundTag) {
				list.addAll(evaluateCompound(nodes, lnbt.getCompound(index)));
			}
			else if (lnbt.get(0) instanceof ListTag) {
				list.addAll(evaluateList(nodes, getListParameters(nodes.get(0)), lnbt.getList(index)));
			}
			else list.add(lnbt.get(index).getAsString());
		}
		
		return list;
	}
	
	
	
	//HELPER METHODS	
	private static boolean isList(String node) {return node.contains("[");}
	
	private static boolean isCompound(String node) {return node.contains("{");}

	private static boolean isQualifiedNode(String node) {return node.contains(":");}

	private static boolean isQualifiedCompound(String node, CompoundTag nbt) {
		String root = rawNode(node);
		String key = node.substring(node.indexOf("{")+1, Math.max(0, node.indexOf(":"))).replaceAll("\"", "");
		String value = node.substring(node.indexOf(":")+1, Math.max(0, node.indexOf("}"))).replaceAll("\"", "");
		boolean test = nbt.contains(root)
				&& nbt.getCompound(root).contains(key)
				&& nbt.getCompound(root).get(key).getAsString()
				.equalsIgnoreCase(value);
		return test;
	}
	
	private static int getListIndex(String node) {
		String rawIndex = getListParameters(node);
		try { return rawIndex.isEmpty() ? -1 : Integer.valueOf(rawIndex);}
		catch(NumberFormatException e) {return -2;}
	}
	
	private static int getQualifiedIndex(String param, ListTag lnbt) {
		if (!isCompound(param)) return -2;
		if (!(lnbt.get(0) instanceof CompoundTag)) return -2;
		String key = param.substring(param.indexOf("{")+1, param.indexOf(":"));
		String value = param.substring(param.indexOf(":")+1, param.indexOf("}"));
		value = rawValue(value);
		for (int i = 0; i < lnbt.size(); i++) {
			CompoundTag element = lnbt.getCompound(i);
			if (element.contains(key) && element.get(key).getAsString().equalsIgnoreCase(value)) return i;
		}
		return -2;
	}
	
	private static String getListParameters(String node) {
		if (isList(node)) {
			int beginIndex = node.indexOf("[")+1;
			int endIndex = node.indexOf("]");
			return node.substring(beginIndex, endIndex);
		}
		return "";
	}
	
	private static String rawNode(String node) {
		if (isList(node)) return node.substring(0, node.indexOf("["));
		else if (isCompound(node)) return node.substring(0, node.indexOf("{"));
		else return node;
		
	}
	
	private static String rawValue(String val) {
		int firstIndex = val.indexOf("\"");
		return val.substring(firstIndex+1, val.indexOf("\"", firstIndex+1));
	}
}
