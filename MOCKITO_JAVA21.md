# Utilisation de Mockito avec Java 21+ et Spring Boot (documentation projet)

## Définition de Mockito

**Mockito** est un framework Java de tests unitaires permettant de créer des objets factices (mocks) pour simuler le comportement de dépendances dans vos classes à tester. Il facilite l’isolation des composants et la vérification des interactions, sans avoir besoin d’implémentations réelles ou d’accès à la base de données.

- Mockito permet de :
  - Simuler le comportement de classes ou interfaces (mocking)
  - Définir des valeurs de retour pour des méthodes
  - Vérifier que certaines méthodes ont été appelées ou non
  - Déclencher des exceptions pour tester la robustesse

## Fonctionnement de Mockito dans les tests

1. **Création des mocks** :
   - Avec l’annotation `@Mock`, Mockito crée un objet factice pour chaque dépendance.
   - Avec `@InjectMocks`, il injecte automatiquement les mocks dans la classe à tester.

2. **Définition du comportement** :
   - On utilise `when(...).thenReturn(...)` pour définir ce que doit retourner une méthode mockée.
   - On peut aussi utiliser `doThrow`, `doNothing`, etc.

3. **Vérification des interactions** :
   - On utilise `verify(...)` pour s’assurer qu’une méthode a bien été appelée (ou non).

4. **Assertions** :
   - On utilise des assertions (`assertThat`, `assertEquals`, etc.) pour vérifier le résultat du test.

## Exemple simple

```java
@Mock
private UserRepository userRepository;
@InjectMocks
private UserService userService;

@Test
void testFindUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(new User("Alice")));
    User user = userService.findUser(1L);
    assertThat(user.getName()).isEqualTo("Alice");
    verify(userRepository, times(1)).findById(1L);
}
```

## Spécificités avec Java 21+

- Depuis Java 17+, Mockito utilise le mode "self-attach" pour permettre le mock des classes finales/statics.
- Cela génère un avertissement lors de l’exécution des tests, mais n’empêche pas leur bon fonctionnement.
- Il n’est plus nécessaire (ni possible) d’ajouter un agent Java spécifique pour Mockito.

## Bonnes pratiques pour la configuration Maven

- Utiliser uniquement la dépendance `mockito-inline` (ou `mockito-core` si besoin) :

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
```

- Ne pas ajouter d’option `<argLine>` pour un agent Mockito dans le plugin Surefire.

## Résultat attendu

- Les tests fonctionnent normalement.
- L’avertissement Mockito est affiché mais n’empêche pas l’exécution.
- Cette configuration est recommandée par Mockito pour Java 17+ et sera supportée jusqu’à ce que le JDK bloque le self-attach.

## Pour aller plus loin

- Surveillez les évolutions de Mockito et du JDK.
- Consultez la documentation officielle Mockito pour les dernières recommandations :
  https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/org/mockito/Mockito.html#0.3
# Explication sur l'exécution des tests avec Mockito et Java 21+

## Résumé

- **Les tests passent parfaitement.**
- L’avertissement affiché par Mockito concernant le chargement dynamique de l’agent Java est normal avec Java 21+.
- Cet avertissement n’empêche pas l’exécution ni la réussite de vos tests unitaires.

## Exemple de message affiché

```
WARNING: A Java agent has been loaded dynamically (.../byte-buddy-agent...)
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Process finished with exit code 0
```

## À retenir

- L’avertissement Mockito est normal sous Java 21+, il n’empêche pas vos tests de fonctionner.
- Gardez simplement la dépendance `mockito-inline` en test, sans configuration d’agent Java dans Maven ou IntelliJ.
- Vous pouvez continuer à utiliser vos tests sans problème.

