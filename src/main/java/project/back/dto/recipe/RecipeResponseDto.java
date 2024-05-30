package project.back.dto.recipe;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.back.entity.recipe.Recipe;

import java.util.List;

@Data
@NoArgsConstructor
public class RecipeResponseDto {
    private String recipeName;
    private String recipeType;
    private String recipeTip;
    private List<RecipeManualDto> recipeManualList;
    private List<RecipePartsDto> recipePartsList;

    @Builder
    public RecipeResponseDto(String recipeName, String recipeType, String recipeTip, List<RecipeManualDto> recipeManualList, List<RecipePartsDto> recipePartsList) {
        this.recipeName = recipeName;
        this.recipeType = recipeType;
        this.recipeTip = recipeTip;
        this.recipeManualList = recipeManualList;
        this.recipePartsList = recipePartsList;
    }

    public static RecipeResponseDto toResponseDto(Recipe recipe, List<RecipeManualDto> recipeManualList, List<RecipePartsDto> recipePartsList) {
        return RecipeResponseDto.builder()
                .recipeName(recipe.getRecipeName())
                .recipeType(recipe.getRecipeType())
                .recipeTip(recipe.getRecipeTip())
                .recipeManualList(recipeManualList)
                .recipePartsList(recipePartsList)
                .build();
    }
}
