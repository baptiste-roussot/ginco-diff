/*
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.gouv.culture.thesaurus.util.template;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.service.rdf.LocalizedString;
import fr.gouv.culture.thesaurus.service.rdf.RdfResource;
import fr.gouv.culture.thesaurus.util.LangUtils;

/**
 * Classe utilitaire pour Apache Velocity permettant de trier des objets de
 * Thésaurus W, notamment les chaînes de caractères régionalisées.
 * <p>
 * Cette implémentation diffère du trieur de valeurs par défaut de Velocity car
 * il permet de trier les objets complexes que sont les chaînes de caractères
 * régionalisées, en prenant en compte différents aspects tels que :
 * <ul>
 * <li>Le tri par rapport à la langue, en prenant en compte des langues
 * prioritaires</li>
 * <li>La prise en compte des parties numériques des libellés</li>
 * </ul>
 * 
 * @author tle
 */
public final class ThesaurusSorter {

	/**
	 * Locales déjà chargés.
	 */
	@SuppressWarnings("unchecked")
	private final Map<String, Locale> locales = new LRUMap();

	/**
	 * Tri des ressources par leur libellé.
	 * 
	 * @param entries
	 *            Ressources à trier par libellé (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @return Ressources triées par libellé, ou <code>null</code> si la liste
	 *         des ressources en entrée est <code>null</code>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection sortEntriesByLabel(final Collection entries,
			final List prioritizedLanguages) {
		return sortEntriesByLabel(entries,
				(Locale[]) prioritizedLanguages.toArray(new Locale[0]));
	}

	/**
	 * Tri des ressources par leur libellé.
	 * 
	 * @param entries
	 *            Ressources à trier par libellé (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @param prioritized
	 *            true pour tenir compte des langues prioritaires, false sinon
	 * @return Ressources triées par libellé, ou <code>null</code> si la liste
	 *         des ressources en entrée est <code>null</code>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection sortEntriesByLabel(final Collection entries,
			final List prioritizedLanguages, final boolean prioritized) {
		return sortEntriesByLabel(entries,
				(Locale[]) prioritizedLanguages.toArray(new Locale[0]),
				prioritized);
	}

	/**
	 * Tri des ressources par leur libellé. Les valeurs <code>null</code> sont
	 * conservées et mises en tête de la collection.
	 * 
	 * @param entries
	 *            Ressources à trier par libellé (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @return Ressources triées par libellé, ou <code>null</code> si la liste
	 *         des ressources en entrée est <code>null</code>
	 */
	public <T extends Entry> Collection<T> sortEntriesByLabel(
			final Collection<T> entries, final Locale[] prioritizedLanguages) {
		return sortEntriesByLabel(entries, prioritizedLanguages, true);
	}

	/**
	 * Tri des ressources par leur libellé. Les valeurs <code>null</code> sont
	 * conservées et mises en tête de la collection.
	 * 
	 * @param entries
	 *            Ressources à trier par libellé (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @param prioritized
	 *            true pour tenir compte des langues prioritaires, false sinon
	 * @return Ressources triées par libellé, ou <code>null</code> si la liste
	 *         des ressources en entrée est <code>null</code>
	 */
	public <T extends Entry> Collection<T> sortEntriesByLabel(
			final Collection<T> entries, final Locale[] prioritizedLanguages,
			final boolean prioritized) {
		Collection<T> sortedEntries;

		if (entries == null) {
			sortedEntries = null;
		} else {
			int nullEntries = 0;
			final List<SortItemWrapper<T>> wrappers = new ArrayList<SortItemWrapper<T>>(
					entries.size());
			final Iterator<T> iterator = entries.iterator();

			while (iterator.hasNext()) {
				final T entry = iterator.next();

				if (entry == null) {
					nullEntries++;
				} else {
					final LocalizedString label = getEntryLabel(entry,
							prioritizedLanguages);
					final String language = label.getLanguage();

					final Collator collator = language == null ? null
							: Collator.getInstance(getLocale(language));

					wrappers.add(new SortItemWrapper<T>(entry, label, collator,
							prioritized ? LangUtils
									.convertLocalesToString(LangUtils
											.expand(prioritizedLanguages))
									: null));
				}
			}

			Collections.sort(wrappers);

			sortedEntries = new ArrayList<T>(wrappers.size() + nullEntries);
			while (nullEntries-- > 0) {
				sortedEntries.add(null);
			}
			for (final SortItemWrapper<T> wrapper : wrappers) {
				sortedEntries.add(wrapper.getData());
			}
		}

		return sortedEntries;
	}

	/**
	 * Tri les chaînes régionalisées par langue, puis par libellé.
	 * 
	 * @param strings
	 *            Chaînes régionalisées à trier (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @return Chaînes régionalisées triées, ou <code>null</code> si les chaînes
	 *         en entrées sont <code>null</code>
	 */
	public Collection<LocalizedString> sortStrings(
			final Collection<LocalizedString> strings,
			final List<String> prioritizedLanguages) {
		return sortStrings(strings,
				LangUtils.convertLocalesToString(LangUtils
						.expand((Locale[]) prioritizedLanguages
								.toArray(new Locale[0]))), null);
	}

	/**
	 * Tri les chaînes régionalisées par langue, puis par libellé.
	 * 
	 * @param strings
	 *            Chaînes régionalisées à trier (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @return Chaînes régionalisées triées, ou <code>null</code> si les chaînes
	 *         en entrées sont <code>null</code>
	 */
	public Collection<LocalizedString> sortStrings(
			final Collection<LocalizedString> strings,
			final String[] prioritizedLanguages) {
		return sortStrings(strings, prioritizedLanguages, null);
	}

	/**
	 * Tri les entrées de la collection sur les valeurs les chaînes
	 * régionalisées par langue, puis par libellé.
	 * <p>
	 * Si la collection en entrée contient des chaines régionalisées, le tri est
	 * fait directement sur celles-ci.
	 * <p>
	 * Si la collection en entrée contient de {@link RdfResource}, le tri est
	 * fait sur la valeur de la propriété nommée "propertyKey"
	 * 
	 * @param strings
	 *            Chaînes régionalisées à trier (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @param propertyKey
	 *            nom de la propriété dans laquelle est récupérée la valeur
	 *            utilisée pour faire le tri. Si null, c'est les valeurs de la
	 *            collection qui sont directement triées
	 * @return Chaînes régionalisées triées, ou <code>null</code> si les chaînes
	 *         en entrées sont <code>null</code>
	 */
	public <T> Collection<T> sortStrings(final Collection<T> strings,
			final List<String> prioritizedLanguages, String propertyKey) {
		return sortStrings(strings,
				LangUtils.convertLocalesToString(LangUtils
						.expand((Locale[]) prioritizedLanguages
								.toArray(new Locale[0]))), propertyKey);		
	}
	/**
	 * Tri les entrées de la collection sur les valeurs les chaînes
	 * régionalisées par langue, puis par libellé.
	 * <p>
	 * Si la collection en entrée contient des chaines régionalisées, le tri est
	 * fait directement sur celles-ci.
	 * <p>
	 * Si la collection en entrée contient de {@link RdfResource}, le tri est
	 * fait sur la valeur de la propriété nommée "propertyKey"
	 * 
	 * @param strings
	 *            Chaînes régionalisées à trier (ou <code>null</code>)
	 * @param prioritizedLanguages
	 *            Langues prioritaires (la valeur <code>null</code> représente
	 *            la langue neutre)
	 * @param propertyKey
	 *            nom de la propriété dans laquelle est récupérée la valeur
	 *            utilisée pour faire le tri. Si null, c'est les valeurs de la
	 *            collection qui sont directement triées
	 * @return Chaînes régionalisées triées, ou <code>null</code> si les chaînes
	 *         en entrées sont <code>null</code>
	 */
	public <T> Collection<T> sortStrings(final Collection<T> strings,
			final String[] prioritizedLanguages, String propertyKey) {
		Collection<T> sortedEntries;

		if (strings == null) {
			sortedEntries = null;
		} else {
			final List<SortItemWrapper<T>> wrappers = new ArrayList<SortItemWrapper<T>>(
					strings.size());
			final Iterator<T> iterator = strings.iterator();

			for (int wrapperId = 0; iterator.hasNext(); wrapperId++) {
				T sortedEntry = iterator.next();
				final LocalizedString sortedValue = getSortedValueFromSortedEntry(
						sortedEntry, propertyKey);
				final String language = sortedValue.getLanguage();
				final Collator collator = language == null ? null : Collator
						.getInstance(getLocale(language));

				wrappers.add(wrapperId, new SortItemWrapper<T>(sortedEntry,
						sortedValue, collator, prioritizedLanguages));
			}

			Collections.sort(wrappers);

			sortedEntries = new ArrayList<T>(wrappers.size());
			for (final SortItemWrapper<T> wrapper : wrappers) {
				sortedEntries.add(wrapper.getData());
			}
		}

		return sortedEntries;
	}

	/**
	 * Récupère la valeur à trier à partir de l'entrée à trier
	 * 
	 * @param entry
	 *            entrée à trier, not null
	 * @param propertyKey
	 *            clé de la propriété contenant la valeur sur laquelle est fait
	 *            le tri. Utilisé seulement si l'entrée à trier est une
	 *            {@link RdfResource}
	 * @return valeur à trier, ou null si aucune valeur trouvée dans la
	 *         propriété
	 */
	private <T> LocalizedString getSortedValueFromSortedEntry(T entry,
			String propertyKey) {
		if (entry instanceof LocalizedString) {
			return (LocalizedString) entry;
		} else if (entry instanceof RdfResource) {
			return getSortedValueFromRdfResource((RdfResource) entry,
					propertyKey);
		} else {
			throw new IllegalArgumentException(
					"this method is only meant to sort collection of LocalizedString or RdfResource");
		}
	}

	private LocalizedString getSortedValueFromRdfResource(RdfResource entry,
			String propertyKey) {
		if (propertyKey == null) {
			throw new IllegalArgumentException(
					"la clé de propriété est obligatoire pour trier une collection de RdfRessource");
		}
		LocalizedString sortedValue = ((RdfResource) entry)
				.getProperty(propertyKey);
		if (sortedValue == null) {
			throw new IllegalArgumentException(
					"aucune valeur ne correspond à la propriété ["
							+ propertyKey
							+ "] pour certaines entrées de la collection à trier : "
							+ entry);
		}
		return sortedValue;
	}

	/**
	 * Charge une locale et la met en cache (si tel n'est pas déjà le cas).
	 * 
	 * @param language
	 *            Langage à charger
	 * @return Locale correspondante
	 */
	private Locale getLocale(final String language) {
		Locale locale = locales.get(language);
		if (locale == null) {
			locale = new Locale(language);
			locales.put(language, locale);
		}
		return locale;
	}

	/**
	 * Renvoie le libellé de l'entrée. Si la priorité du choix de langages peut
	 * être respectée, alors la première langue disponible dans l'entrée sera
	 * utilisée. Sinon, l'URI de l'entrée sera utilisée.
	 * 
	 * @param entry
	 *            Entrée dont on souhaite récupérer le libellé
	 * @param prioritizedLanguages
	 *            Ordre de priorité des langues souhaitées
	 * @return Libellé de l'entrée, ou son URI si aucun libellé n'a été trouvé
	 */
	private LocalizedString getEntryLabel(Entry entry,
			Locale[] prioritizedLanguages) {
		LocalizedString label = null;
		for (int currentLanguageId = 0; label == null
				&& currentLanguageId < prioritizedLanguages.length; currentLanguageId++) {
			label = entry.getLabel(prioritizedLanguages[currentLanguageId]);
		}

		if (label == null) {
			label = new LocalizedString(entry.getUri(), null);
		}

		return label;
	}

}
