package project.back.controller.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.back.dto.recipe.api.RecipeApiEntityDto;
import project.back.service.recipe.RecipeApiService;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeApiController {

    private final RecipeApiService recipeService;

    @GetMapping("/menu")
    public Mono<List<RecipeApiEntityDto>> fetchAndSaveRecipes(@RequestParam("menu") String menu) {
        return recipeService.fetchAndSaveRecipesByMenu(menu);
    }

    @GetMapping("/type")
    public Mono<List<RecipeApiEntityDto>> fetchAndSaveRecipesByType(@RequestParam("type") String type) {
        return recipeService.fetchAndSaveRecipesByType(type);
    }
}
