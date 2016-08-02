package com.ery.base.support.utils.hash;

import java.io.IOException;

import com.ery.base.support.utils.hash.HashTable.HashNode;

public interface JudgeHash {
	public boolean addNode(HashNode node) throws IOException;

	public boolean contains(HashNode node) throws IOException;

	public void close();

	public void clear();
}
