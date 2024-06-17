package project.back.entity.recipe;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.back.entity.member.Member;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recipeId;

    @Column(nullable = true)
    private Long recipeApiNo;

    @Column(nullable = false)
    private String recipeName;

    @Column(nullable = false)
    private String recipeType;

    @Column(nullable = true)
    private String recipeTip;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecipeManual> recipeManual;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecipeParts> recipeParts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @Builder(toBuilder = true)
    public Recipe(Long recipeApiNo, String recipeName, String recipeType, String recipeTip, List<RecipeManual> recipeManual, List<RecipeParts> recipeParts, Member member) {
        this.recipeApiNo = recipeApiNo;
        this.recipeName = recipeName;
        this.recipeType = recipeType;
        this.recipeTip = recipeTip;
        this.recipeManual = recipeManual;
        this.recipeParts = recipeParts;
        this.member = member;
    }
}
