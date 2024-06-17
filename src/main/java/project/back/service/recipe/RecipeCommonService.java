package project.back.service.recipe;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.back.dto.ApiResponse;
import project.back.dto.recipe.ManualResDto;
import project.back.dto.recipe.RecipeListDto;
import project.back.dto.recipe.RecipePartsDto;
import project.back.dto.recipe.RecipeResponseDto;
import project.back.entity.recipe.Recipe;
import project.back.entity.recipe.RecipeManual;
import project.back.etc.commonException.NoContentFoundException;
import project.back.etc.recipe.RecipeMessage;
import project.back.repository.recipe.RecipeManualRepository;
import project.back.repository.recipe.RecipePartsRepository;
import project.back.repository.recipe.RecipeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipeCommonService {
    private final RecipeRepository recipeRepository;
    private final RecipeManualRepository manualRepository;
    private final RecipePartsRepository partsRepository;

    /**
     * 레시피 페이징 처리해서 가져오기
     *
     * @param page 요청할 페이지 번호
     * @param size 페이지 크기
     * @return 페이징 처리된 레시피 목록과 마지막 단계의 이미지를 ApiResponse 형태로 반환
     * @throws NoContentFoundException 레시피 목록이 비어있을 때 발생
     * @throws EntityNotFoundException 레시피 메뉴얼이나 이미지를 찾을 수 없을 때 발생
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<RecipeListDto>> getAllRecipe(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recipeId").ascending());

        List<RecipeListDto> recipeResDto = recipeRepository.findAll(pageable)
                .map(this::convertToRecipeDto)
                .toList();
        if (recipeResDto.isEmpty()) {
            throw new NoContentFoundException(RecipeMessage.EMPTY_RECIPE_LIST.getMessage());
        }
        return ApiResponse.success(recipeResDto, RecipeMessage.LOADED_RECIPE_LIST.getMessage());
    }

    /**
     * 레시피와 해당 레시피의 메뉴얼 마지막 단계 이미지를 반환
     */
    private RecipeListDto convertToRecipeDto(Recipe recipe) {
        Long step = manualRepository.findLastStepByRecipeId(recipe.getRecipeId())
                .orElseThrow(() -> new EntityNotFoundException(RecipeMessage.NOT_FOUND_RECIPE_MANUAL.getMessage()));
        Optional<RecipeManual> manualImg = manualRepository.findManualImgUrlByRecipeRecipeIdAndStep(recipe.getRecipeId(), step);
        return RecipeListDto.recipeResList(recipe, manualImg);
    }

    /**
     * 해당 레시피 정보 가져오기
     *
     * @param recipeId 가져올 레시피 번호
     * @return 레시피 정보를 ApiResponse 형태로 반환
     * @throws EntityNotFoundException 레시피를 찾을 수 없을 때 발생
     */
    public ApiResponse<RecipeResponseDto> getRecipeId(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(RecipeMessage.NOT_FOUND_RECIPE.getMessage()));
        List<ManualResDto> manualList = manualRepository.findByRecipeRecipeId(recipeId)
                .stream()
                .map(ManualResDto::manualResDto)
                .toList();

        List<RecipePartsDto> partsList = partsRepository.findByRecipeRecipeId(recipeId)
                .stream()
                .map(RecipePartsDto::partsDto)
                .toList();

        return ApiResponse.success(
                RecipeResponseDto.toResponseDto(recipe, manualList, partsList),
                RecipeMessage.LOADED_RECIPE.getMessage()
        );
    }
}

