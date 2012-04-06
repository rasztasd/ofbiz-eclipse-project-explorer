package org.ofbiz.plugin.model;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.ofbiz.plugin.ofbiz.IEntity;
import org.ofbiz.plugin.ofbiz.MemberEntity;
import org.ofbiz.plugin.ofbiz.Project;
import org.ofbiz.plugin.ofbiz.ViewEntity;
import org.ofbiz.plugin.ofbiz.ViewLink;
import org.ofbiz.plugin.ofbiz.util.OfbizSwitch;

public class ViewEntitySqlHelper {
	private static IEntity getIEntityByname(final String entityName) {
		Project project = OfbizModelSingleton.get().findActiveEclipseProject();
		if (project != null) {
			TreeIterator<EObject> eAllContents = project.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				OfbizSwitch<IEntity> ofbizSwitch = new OfbizSwitch<IEntity>() {
					
					@Override
					public IEntity caseIEntity(IEntity object) {
						if (object.getName().equals(entityName)) {
							return object;
						}
						return null;
					}
					
				};
				
				IEntity doSwitch = ofbizSwitch.doSwitch(eObject);
				if (doSwitch != null) {
					return doSwitch;
				}
			}
		}
		return null;
	}
	private static String getEntityName(ViewEntity viewEntity, String entityAlias) {
		for (MemberEntity memberEntity : viewEntity.getMemberEntities()) {
			if (memberEntity.getEntityAlias().equals(entityAlias)) {
				return memberEntity.getEntityName();
			}
		}
		return null;
	}
	public static String makeFromClause(IEntity viewEntity) {
		StringBuffer sql = new StringBuffer(" FROM ");

		if (viewEntity instanceof ViewEntity) {
			ViewEntity modelViewEntity = (ViewEntity) viewEntity;
			boolean useParenthesis = true;

			Set joinedAliasSet = new TreeSet();

			StringBuffer openParens = null;
			if (useParenthesis) openParens = new StringBuffer();
			StringBuffer restOfStatement = new StringBuffer();

			for (int i = 0; i < modelViewEntity.getViewLinks().size(); i++) {
				if (i > 0 && useParenthesis) openParens.append('(');

				ViewLink viewLink = modelViewEntity.getViewLinks().get(i);

				IEntity linkEntity = getIEntityByname(getEntityName(modelViewEntity, viewLink.getEntityAlias()));
				IEntity relLinkEntity = getIEntityByname(getEntityName(modelViewEntity, viewLink.getRelEntityAlias()));

				if (i == 0) {
					restOfStatement.append(makeViewTable(linkEntity));
					restOfStatement.append(" ");
					restOfStatement.append(viewLink.getEntityAlias());
					joinedAliasSet.add(viewLink.getEntityAlias());
				}
				joinedAliasSet.add(viewLink.getRelEntityAlias());

				if (viewLink.isOptional()) {
					restOfStatement.append(" LEFT OUTER ");
				} else {
					restOfStatement.append(" INNER ");
				}

				restOfStatement.append(" JOIN ");

				restOfStatement.append(makeViewTable(relLinkEntity));
				restOfStatement.append(" ");
				restOfStatement.append(viewLink.getRelEntityAlias());
				restOfStatement.append(" ON ");

				StringBuffer condBuffer = new StringBuffer();

//				for (int j = 0; j < viewLink.getKeyMapsSize(); j++) {
//					ModelKeyMap keyMap = viewLink.getKeyMap(j);
//					ModelField linkField = linkEntity.getField(keyMap.getFieldName());
//					ModelField relLinkField = relLinkEntity.getField(keyMap.getRelFieldName());
//
//					if (condBuffer.length() > 0) {
//						condBuffer.append(" AND ");
//					}
//
//					condBuffer.append(viewLink.getEntityAlias());
//					condBuffer.append(".");
//					condBuffer.append(filterColName(linkField.getColName()));
//
//					condBuffer.append(" = ");
//
//					condBuffer.append(viewLink.getRelEntityAlias());
//					condBuffer.append(".");
//					condBuffer.append(filterColName(relLinkField.getColName()));
//				}
				// Beginning of arvato customization
				// Modification date: 08 apr 2009
				// extra join parameters can be added for model
//				if (condBuffer.length() == 0) {
//					throw new GenericModelException("No view-link/join key-maps found for the " + viewLink.getEntityAlias() + " and the " + viewLink.getRelEntityAlias() + " member-entities of the " + modelViewEntity.getEntityName() + " view-entity.");
//				}
//				else {
//					for (ModelBoundField boundField : (List<ModelBoundField>) viewLink.getBoundMapsCopy()) {
//						ModelField relLinkField = relLinkEntity.getField(boundField.getRelatedFieldName());
//						if (relLinkField == null) {
//							throw new GenericModelException("Invalid related field name in view-link bound-field for the " + viewLink.getEntityAlias() + " and the " + viewLink.getRelEntityAlias() + " member-entities of the " + modelViewEntity.getEntityName() + " view-entity; the field [" + boundField.getRelatedFieldName() + "] does not exist on the [" + relLinkEntity.getEntityName() + "] entity.");
//						}
//
//						if (condBuffer.length() > 0) {
//							condBuffer.append(" AND ");
//						}
//
//						condBuffer.append(viewLink.getRelEntityAlias());
//						condBuffer.append(".");
//						condBuffer.append(filterColName(relLinkField.getColName()));
//
//						condBuffer.append(" = ");
//
//						if (UtilValidate.isNotEmpty(boundField.getRelatedFieldValue())) {
//							condBuffer.append("'");
//							condBuffer.append(boundField.getRelatedFieldValue());
//							condBuffer.append("'");
//						}
//						else {
//							ModelField linkField = linkEntity.getField(boundField.getFieldName());
//							if (linkField == null) {
//								throw new GenericModelException("Invalid field name in view-link bound-field for the " + viewLink.getEntityAlias() + " and the " + viewLink.getRelEntityAlias() + " member-entities of the " + modelViewEntity.getEntityName() + " view-entity; the field [" + boundField.getFieldName() + "] does not exist on the [" + linkEntity.getEntityName() + "] entity.");
//							}
//							condBuffer.append(viewLink.getEntityAlias());
//							condBuffer.append(".");
//							condBuffer.append(filterColName(linkField.getColName()));
//						}
//					}
//					// End of arvato customization
//				}
				restOfStatement.append(condBuffer.toString());

				// don't put ending parenthesis
				if (i < (modelViewEntity.getViewLinks().size() - 1) && useParenthesis) restOfStatement.append(')');
			}

			if (useParenthesis) sql.append(openParens.toString());
			sql.append(restOfStatement.toString());

			// handle tables not included in view-link
//			Iterator meIter = modelViewEntity.getMemberModelMemberEntities().entrySet().iterator();
			boolean fromEmpty = restOfStatement.length() == 0;

//			while (meIter.hasNext()) {
//				Map.Entry entry = (Map.Entry) meIter.next();
//				viewEntity fromEntity = modelViewEntity.getMemberviewEntity((String) entry.getKey());
//
//				if (!joinedAliasSet.contains((String) entry.getKey())) {
//					if (!fromEmpty) sql.append(", ");
//					fromEmpty = false;
//
//					sql.append(makeViewTable(fromEntity));
//					sql.append(" ");
//
//					ViewEntity.ModelMemberEntity memberEntity = modelViewEntity.getMemberModelMemberEntity((String) entry.getKey());
//					if (UtilValidate.isNotEmpty(memberEntity.getOptions())) {
//						restOfStatement.append(" WITH(").append(memberEntity.getOptions()).append(")");
//					} else if (unlockRead) {
//						restOfStatement.append(" WITH (NOLOCK)");
//					}
//
//					sql.append((String) entry.getKey());
//				}
//			}


		} else {
			//this is the case when a plain entity is used.
			sql.append(viewEntity.getName());
		}
		return sql.toString();
	}
	public static String makeViewTable(IEntity modelEntity) {
            StringBuffer sql = new StringBuffer("(SELECT ");
            sql.append(" * ");
            sql.append(makeFromClause(modelEntity));
//	            String viewWhereClause = makeViewWhereClause(modelEntity, datasourceInfo);
			    // End of arvato customization
//	            if (UtilValidate.isNotEmpty(viewWhereClause)) {
//	                sql.append(" WHERE ");
//	                sql.append(viewWhereClause);
//	            }
//	            ModelViewEntity modelViewEntity = (ModelViewEntity)modelEntity;
//	            String groupByString = modelViewEntity.colNameString(modelViewEntity.getGroupBysCopy(), ", ", "", false);
//	            if (UtilValidate.isNotEmpty(groupByString)) {
//	                sql.append(" GROUP BY ");
//	                sql.append(groupByString);
//	            }

//	            sql.append(")");
            return sql.toString();
	        
	    }
}
