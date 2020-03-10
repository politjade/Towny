package com.palmergames.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Dynamic trie structure that can add/remove keys and recursively get matching strings for a key 
 * 
 * @author stzups
 */
public class Trie {

	private static final int MAX_RETURNS = 100;
	/**
	 * TrieNode implementation that handles any character and keeps track of its own children and character
	 */
	public static class TrieNode {
		List<TrieNode> children = new ArrayList<>();
		char character;
		boolean endOfWord = false;

		TrieNode(char character) {
			this.character = character;
		}
	}

	private final TrieNode root;

	/**
	 * Constructor that creates a new trie with a null root
	 */
	public Trie() {
		root = new TrieNode(Character.MIN_VALUE);
	}

	/**
	 * Adds and links new TrieNodes to the trie for each character in the string
	 * 
	 * @param key key to add to trie, can be longer than one character
	 */
	public void addKey(String key) {
		// Current trieNode to crawl through
		TrieNode trieNode = root;

		// Loop through each character of key
		for (int i = 0; i < key.length(); i++) {
			char index = key.charAt(i);

			TrieNode lastNode = trieNode;
			trieNode = null;
			for (TrieNode node : lastNode.children) {
				if (node.character == index) {
					trieNode = node;
					break;
				}
			}

			if (trieNode == null) {
				trieNode = new TrieNode(index);
				lastNode.children.add(trieNode); // Put this node as one of lastNode's children

				if (i == key.length() - 1) { // Check if this is the last character of the key, indicating a word ending
					trieNode.endOfWord = true;
				}
			}
		}
	}

	/**
	 * Removes TrieNodes for a key
	 * 
	 * @param key key to remove
	 */
	public void removeKey(String key) {
		// Current trieNode to crawl through
		TrieNode trieNode = root;
		Queue<TrieNode> found = Collections.asLifoQueue(new LinkedList<>());

		// Loop through each character of key
		for (int i = 0; i < key.length(); i++) {
			char index = key.charAt(i);
			TrieNode lastNode = trieNode;
			TrieNode node = null;
			for (TrieNode nodeLoop : lastNode.children) {
				if (nodeLoop.character == index) {
					node = nodeLoop;
				}
			}

			if (node != null) {
				trieNode = node;
				found.add(trieNode);
				if (i == key.length()-1) { // Check if this is the last character of the key, indicating a word ending
					foundLoop:
					for (TrieNode trieNode1 : found) {
						Iterator<TrieNode> iterator = trieNode1.children.iterator();
						while (iterator.hasNext()) {
							TrieNode child = iterator.next();
							if (found.contains(child) && child.children.size() < 2) { // Only remove if in found and there are one or no children
								iterator.remove();
							} else {
								break foundLoop;
							}
						}
					}
				}
			} else {
				break; // This shouldn't happen
			}
		}
	}

	/**
	 * Gets all matching strings and their children for a key
	 * 
	 * @param key string to search for in tree
	 * @return matching strings and their children
	 */
	public List<String> getStringsFromKey(String key) {
		// Empty key means find all nodes, starting from the root node
		if (key.length() == 0) {
			return getChildrenStrings(root, new ArrayList<>());
		}

		List<String> strings = new ArrayList<>();

		Map<TrieNode, String> nodes = new HashMap<>(); // Contains a key for each TrieNode
		nodes.put(root, ""); // Start with the root node

		for (int i = 0; i < key.length(); i++) {
			Map<TrieNode, String> newNodes = new HashMap<>(); // An updated version of nodes, will not contain the old values
			char index = Character.toLowerCase(key.charAt(i));

			for (Map.Entry<TrieNode, String> entry : nodes.entrySet()) { // Loop through the old nodes

				for (TrieNode node : entry.getKey().children) {

					if (Character.toLowerCase(node.character) == index) {
						String realKey = entry.getValue()+node.character;
						newNodes.put(node, realKey); // entry.getValue is the old key for the node, for example "bana" as entry.getValue() and "n" as listNode.character resulting in "banan" for listNode

						if (i == key.length() - 1) { // Check if this is the last character of the key, indicating a word ending. From here we need to find all the possible children

							for (String string : getChildrenStrings(node, new ArrayList<>())) { // Recursively find all children
								strings.add(realKey + string); // Add the key to the front of each child string
							}
						}
					}
				}
			}
			nodes = newNodes;
		}

		return strings;
	}

	/**
	 * Recursively find all children of a TrieNode, and add to a list of strings
	 * 
	 * @param find the current TrieNode to search through its own children
	 * @param found strings that have already been found
	 * @return strings of all children found, with this TrieNode's character in front of each string
	 */
	private static List<String> getChildrenStrings(TrieNode find, List<String> found) {

		for (TrieNode trieNode : find.children) { // Loop through each child

			if (found.size() + 1 > MAX_RETURNS) {
				return found;
			}

			if (!trieNode.endOfWord) { // Not the end of the word, so loop through all children
				for (String string : getChildrenStrings(trieNode, new ArrayList<>())) {
					if (found.size() + 1 > MAX_RETURNS) {
						return found;
					} else {
						found.add(trieNode.character + string);
					}
				}
			} else { // End of word, so just add this TrieNode's character
				found.add(String.valueOf(trieNode.character));
			}
		}

		return found;
	}
}
