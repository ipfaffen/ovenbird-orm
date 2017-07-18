package com.ipfaffen.ovenbird.model.criteria;

import static com.ipfaffen.ovenbird.model.ModelConstants.ALIAS_PREFFIX;
import static com.ipfaffen.ovenbird.model.ModelConstants.JOIN_MAX_DEPTH;
import static com.ipfaffen.ovenbird.model.ModelConstants.MAIN_TABLE_ALIAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.ipfaffen.ovenbird.model.ModelUtil;
import com.ipfaffen.ovenbird.model.util.JoinColumnField;

/**
 * @author Isaias Pfaffenseller
 */
public class CriteriaJoin {

	protected HashMap<String, JoinColumnField> joinFields;
	private List<JoinColumnField> joinFieldList;
	private Integer mainAliasCounter;
	
	public CriteriaJoin() {
		joinFields = new HashMap<String, JoinColumnField>();
		mainAliasCounter = -1;
	}

	public void clearJoins() {
		joinFields = new HashMap<String, JoinColumnField>();
		joinFieldList = null;
		mainAliasCounter = -1;
	}

	/**
	 * @param baseClass
	 * @param fetches
	 * @return last join column field.
	 */
	protected JoinColumnField addFetch(Class<?> baseClass, String[] fetches) {
		String baseIdentifier = null;

		// Build base column (level 1).
		JoinColumnField baseJoinField = new JoinColumnField();
		baseJoinField.setAlias(MAIN_TABLE_ALIAS);
		baseJoinField.setAliasHandler(new Integer[JOIN_MAX_DEPTH]);
		baseJoinField.setAliasIndex(mainAliasCounter);

		for(int i = 0; i < fetches.length; i++) {
			String fetch = fetches[i];
			String identifier = ((baseIdentifier != null) ? baseIdentifier + "." : "") + fetch;

			baseIdentifier = identifier;

			// If relationship already exists then just feed BaseJoinField to use in next index.
			if(joinFields.containsKey(identifier)) {
				baseJoinField = joinFields.get(identifier);
				baseClass = baseJoinField.getTable().getType();
				continue;
			}

			Integer[] aliasHandler = getAliasHandler(baseJoinField, i);
			JoinColumnField joinField = ModelUtil.getJoinField(baseClass, fetch);

			joinField.setIdentifier(baseIdentifier);
			joinField.setDepthLevel(i);
			joinField.setBaseJoinField(baseJoinField);
			joinField.setAliasHandler(aliasHandler);
			joinField.setAlias(getAlias(aliasHandler));

			joinFields.put(identifier, joinField);

			baseClass = joinField.getTable().getType();
			baseJoinField = joinField;
		}
		return baseJoinField;
	}

	/**
	 * @param joinField
	 * @param index
	 * @return
	 */
	private Integer[] getAliasHandler(JoinColumnField joinField, int index) {
		Integer[] aliasHandler = joinField.getAliasHandler();

		aliasHandler[index] = joinField.getAliasIndex() + 1;
		for(int i = (index + 1); i < aliasHandler.length; i++) {
			aliasHandler[i] = null;
		}

		if(index == 0) {
			mainAliasCounter++;
		}

		joinField.setAliasIndex(aliasHandler[index]);
		return aliasHandler;
	}

	/**
	 * @param aliasHandler
	 * @return
	 */
	private String getAlias(Integer[] aliasHandler) {
		StringBuilder alias = new StringBuilder(ALIAS_PREFFIX);
		for(int i = 0; i < aliasHandler.length; i++) {
			if(aliasHandler[i] == null) {
				break;
			}
			if(i > 0) {
				alias.append("_");
			}
			alias.append(aliasHandler[i]);
		}
		return alias.toString();
	}

	/**
	 * @return - list of join field sorted by identifier.
	 */
	protected List<JoinColumnField> getJoinFields() {
		if(joinFieldList == null) {
			joinFieldList = new ArrayList<JoinColumnField>(joinFields.values());
			Collections.sort(joinFieldList, new Comparator<JoinColumnField>() {
				@Override
				public int compare(JoinColumnField o1, JoinColumnField o2) {
					return o1.getIdentifier().compareTo(o2.getIdentifier());
				}
			});
		}
		return joinFieldList;
	}
}