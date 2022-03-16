package net.openid.conformance.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.jwk.JWKSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapSanitiser {

	public static class LeafNode {

		private Map<String, Object> owner;
		private Object property;
		private String key;
		private LeafType type;

		public LeafNode(Map<String, Object> owner, Object property, String key, LeafType type) {
			this.owner = owner;
			this.property = property;
			this.key = key;
			this.type = type;
		}

		public Object getProperty() {
			return property;
		}

		public void replace(Object replacement) {
			owner.put(key, replacement);
		}

		public LeafType getType() {
			return type;
		}

		public String getKey() {
			return key;
		}

		public void visit(MapLeafNodeVisitor sanitiser) {
			sanitiser.accept(this);
		}

	}

	private final Set<MapLeafNodeVisitor> sanitisers;


	public MapSanitiser(Set<MapLeafNodeVisitor> sanitisers) {
		this.sanitisers = sanitisers;
	}

	public void sanitise(Map<String, Object> map) {
		Set<LeafNode> leafNodes = findLeafNodes(map);
		sanitise(leafNodes);
	}

	public Set<LeafNode> findLeafNodes(Map<String, Object> map) {
		Set<LeafNode> leafNodes = new HashSet<>();
		findLeafNodes(leafNodes, map);
		return leafNodes;
	}

	public void sanitise(Set<LeafNode> leafNodes) {
		sanitisers.stream()
			.forEach(s -> runSanitiser(s, leafNodes));
	}

	private void runSanitiser(MapLeafNodeVisitor sanitiser, Set<LeafNode> leafNodes) {
		leafNodes.stream()
			.forEach(n -> n.visit(sanitiser));
	}


	private void findLeafNodes(Set<LeafNode> leaves, Map<String, Object> object) {
		for(String key: object.keySet()) {
			Object element = object.get(key);
			if(element instanceof JWKSet) {
				LeafNode wrapper = new LeafNode(object, element, key, LeafType.JWKS);
				leaves.add(wrapper);
				continue;
			}
			if(element instanceof JsonObject) {
				if(probablyJwks((JsonObject) element)){
					LeafNode wrapper = new LeafNode(object, element, key, LeafType.JWKS);
					leaves.add(wrapper);
					continue;
				}
			}
			if(element instanceof Map) {
				findLeafNodes(leaves, (Map<String, Object>) element);
				continue;
			}

			LeafNode wrapper = new LeafNode(object, element, key, LeafType.PRIVATE_KEY);
			leaves.add(wrapper);
		}
	}

	private boolean probablyJwks(JsonObject maybeJwks) {
		if(maybeJwks.keySet().size() != 1) {
			return false;
		}
		if(!maybeJwks.has("keys")) {
			return false;
		}
		JsonElement keys = maybeJwks.get("keys");
		return keys.isJsonArray();
	}

}
