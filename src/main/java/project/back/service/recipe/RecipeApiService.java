package project.back.service.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.back.configuration.WebClientConfig;
import project.back.dto.recipe.RecipeResponseDto;
import project.back.dto.recipe.api.RecipeApiDto;
import project.back.dto.recipe.api.RecipeApiResponse;
import project.back.entity.recipe.Recipe;
import project.back.repository.recipe.RecipeRepository;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeApiService {

    private static final int PAGE_SIZE = 100;
    private final WebClientConfig webClientConfig;
    private final RecipeRepository recipeRepository;
    private final RecipeDataAccessService dataAccessService;
    private final Map<String, Integer> menuPageMap = new HashMap<>();
    private final Map<String, Integer> typePageMap = new HashMap<>();

    public Mono<List<RecipeResponseDto>> fetchAndSaveRecipesByMenu(String menu) {
        int currentPage = menuPageMap.getOrDefault(menu, 1);
        return fetchMenu("RCP_NM", menu, currentPage, PAGE_SIZE)
                .flatMap(response -> saveRecipesFromApiResponse(response)
                        .doOnNext(savedRecipes -> menuPageMap.put(menu, currentPage + 1))
                        .map(this::convertToRecipeResponseDtos));
    }

    public Mono<List<RecipeResponseDto>> fetchAndSaveRecipesByType(String type) {
        int currentPage = typePageMap.getOrDefault(type, 1);
        return fetchMenu("RCP_PAT2", type, currentPage, PAGE_SIZE)
                .flatMap(response -> saveRecipesFromApiResponse(response)
                        .doOnNext(savedRecipes -> typePageMap.put(type, currentPage + 1))
                        .map(this::convertToRecipeResponseDtos));
    }

    private Mono<RecipeApiResponse> fetchMenu(String key, String value, int page, int size) {
        int start = (page - 1) * size + 1;
        int end = page * size;

        return webClientConfig.recipeWebClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path(start + "/" + end + "/" + key + "=" + value)
                        .build())
                .retrieve()
                .bodyToMono(RecipeApiResponse.class);
    }

    private Mono<List<Recipe>> saveRecipesFromApiResponse(RecipeApiResponse response) {
        List<RecipeApiDto> apiDtos = response.getCookrcp01().getRow();
        Set<Long> existingRecipeApiNos = new HashSet<>(recipeRepository.findAllRecipeApiNos());
        List<Recipe> savedRecipes = new ArrayList<>();

        apiDtos.stream()
                .filter(apiDto -> !existingRecipeApiNos.contains(apiDto.getRecipeApiNo()))
                .forEach(apiDto -> {
                    Recipe recipe = convertToRecipeEntity(apiDto);
                    recipeRepository.save(recipe);
                    dataAccessService.saveManuals(apiDto.getManual(), recipe);
                    dataAccessService.saveParts(apiDto.extractRecipeParts(), recipe);
                    savedRecipes.add(recipe);
                });
        return Mono.just(savedRecipes);
    }

    private Recipe convertToRecipeEntity(RecipeApiDto apiDto) {
        return Recipe.builder()
                .recipeApiNo(apiDto.getRecipeApiNo())
                .recipeName(apiDto.getRecipeName())
                .recipeType(apiDto.getRecipeType())
                .recipeTip(apiDto.getRecipeTip())
                .build();
    }

    private List<RecipeResponseDto> convertToRecipeResponseDtos(List<Recipe> recipes) {
        return recipes.stream()
                .map(recipe -> RecipeResponseDto.toResponseDto(
                        recipe,
                        dataAccessService.findManualsByRecipe(recipe),
                        dataAccessService.findPartsByRecipe(recipe)
                ))
                .toList();
    }
}