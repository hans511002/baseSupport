package com.sobey.jcg.support.utils.hash;

import java.io.IOException;

import com.sobey.jcg.support.utils.hash.HashTable.HashNode;

public interface JudgeHash {
	public boolean addNode(HashNode node) throws IOException;

	public boolean contains(HashNode node) throws IOException;

	public void close();

	public void clear();
}
