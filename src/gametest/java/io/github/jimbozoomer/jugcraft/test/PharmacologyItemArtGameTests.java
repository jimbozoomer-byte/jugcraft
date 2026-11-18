package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.pharmacology.*;
import io.github.jimbozoomer.jugcraft.weapons.FieldChemistry;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

/** Real dose/packaging conservation after replacing drink aliases with the planned product forms. */
public class PharmacologyItemArtGameTests {
	private static ServerPlayer actor(GameTestHelper h) {var p=TestPlayers.survivor(h);p.getInventory().clearContent();return p;}
	@GameTest public void packetsAndStimulantTabletsKeepUseTimeAndProduceNoGlass(GameTestHelper h) {
		for(var item:List.of(Pharmacology.REHYDRATION_DRINK,FieldChemistry.STIMULANT)) {
			var p=actor(h);var stack=new ItemStack(item,2);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
			var use=stack.get(DataComponents.CONSUMABLE);
			h.assertTrue(use.animation()==ItemUseAnimation.EAT && use.consumeSeconds()==1.6F,"Packet/tablet uses its original 1.6-second duration without drink pose");
			p.setItemInHand(InteractionHand.MAIN_HAND,stack.finishUsingItem(p.level(),p));
			h.assertTrue(p.getMainHandItem().is(item) && p.getMainHandItem().getCount()==1 && p.getInventory().countItem(Items.GLASS_BOTTLE)==0,"A paid packet/tablet consumes exactly one and cannot create a bottle");
			h.assertTrue(Pharmacology.strain(p)>0,"Real dose accounting is still charged");
		}
		h.succeed();
	}
	@GameTest public void medicalDevicesKeepTheirPaidGlassCartridgeReturnAndTiming(GameTestHelper h) {
		for(String id:List.of("adrenaline","salbutamol","naloxone")) {
			var p=actor(h);var item=SterilePharmacy.item(id);var stack=new ItemStack(item,2);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
			var use=stack.get(DataComponents.CONSUMABLE);
			h.assertTrue(use.animation()==ItemUseAnimation.NONE && use.consumeSeconds()==1.6F,"Device has a stable non-drinking use pose and unchanged duration");
			p.setItemInHand(InteractionHand.MAIN_HAND,stack.finishUsingItem(p.level(),p));
			h.assertTrue(p.getMainHandItem().getCount()==1 && p.getInventory().countItem(Items.GLASS_BOTTLE)==1,"Existing sterile cartridge returns one glass container, never the device itself");
		}
		h.succeed();
	}
	@GameTest public void sachetAndTabletAcquisitionPaysPaperOrBinderInsteadOfUnreturnedGlass(GameTestHelper h) {
		var salt=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("salt"));
		var input=CraftingInput.of(3,2,List.of(new ItemStack(salt),new ItemStack(Items.SUGAR),new ItemStack(salt),new ItemStack(Items.PAPER),new ItemStack(Items.WATER_BUCKET),new ItemStack(Items.PAPER)));
		var result=h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value().assemble(input);
		h.assertTrue(result.is(Pharmacology.REHYDRATION_DRINK) && result.getCount()==2,"Ordinary reachable paper/salt/sugar/water craft still yields two sachets");
		var compact=FluidRecipes.entries(h.getLevel().getServer(),MachineKind.CHEMICAL_REACTOR).stream().filter(e->e.recipe().results().stream().anyMatch(r->r.create().is(FieldChemistry.STIMULANT))).findFirst().orElseThrow().recipe();
		h.assertTrue(compact.items().stream().anyMatch(i->i.ingredient().test(new ItemStack(Pharmacology.PHARMACEUTICAL_STARCH))) && compact.items().stream().noneMatch(i->i.ingredient().test(new ItemStack(Items.GLASS_BOTTLE))),"Existing compact Stimulant route pays a reachable binder, with no lost bottle input");
		h.succeed();
	}
	@GameTest public void advancedFinishedFormsKeepOneDoseTimingAndPaidContainerReturns(GameTestHelper h) {
		for(String id:"amphetamine_xr caffeine_xr dmt fentanyl_patch high_proof_spirits lsd masterwork_reserve mescaline methylphenidate_xr modafinil_xr moonshine psilocybin refined_spirits reserve_whiskey thc".split(" ")) {
			var p=actor(h);var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id(id));
			var stack=new ItemStack(item,2);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
			boolean drink=List.of("moonshine","refined_spirits","reserve_whiskey","high_proof_spirits","masterwork_reserve").contains(id);
			var use=stack.get(DataComponents.CONSUMABLE);
			h.assertTrue(use.consumeSeconds()==(drink||id.equals("fentanyl_patch")?1.6F:.8F),"Existing duration is preserved for "+id);
			h.assertTrue(use.animation()==(drink?ItemUseAnimation.DRINK:id.equals("fentanyl_patch")?ItemUseAnimation.NONE:ItemUseAnimation.EAT),"Native use fits the finished form: "+id);
			p.setItemInHand(InteractionHand.MAIN_HAND,stack.finishUsingItem(p.level(),p));
			h.assertTrue(p.getMainHandItem().is(item)&&p.getMainHandItem().getCount()==1,"Exactly one authorized dose is spent: "+id);
			h.assertTrue(p.getInventory().countItem(Items.GLASS_BOTTLE)==(drink?1:0),"Only a paid beverage bottle returns glass: "+id);
			h.assertTrue(Pharmacology.strain(p)>0,"Existing shared accounting still charges the real use: "+id);
		}
		h.succeed();
	}
	@GameTest public void lsdPaperDoseFormulationPaysFourPaperAndRetainsItsExistingFormulaAndYield(GameTestHelper h) {
		var recipes=FluidRecipes.entries(h.getLevel().getServer(),io.github.jimbozoomer.jugcraft.machine.form.IndustrialForms.FORMULATION_PRESS.form().family());
		var recipe=recipes.stream().map(FluidRecipes.Entry::recipe).filter(r->r.results().stream().anyMatch(o->o.create().is(PsychedelicPharmacy.item("lsd")))).findFirst().orElseThrow();
		h.assertTrue(recipe.items().stream().anyMatch(i->i.count()==4&&i.ingredient().test(new ItemStack(Items.PAPER))),"Four paper pay for the four finished paper doses");
		h.assertTrue(recipe.items().stream().noneMatch(i->i.ingredient().test(new ItemStack(PsychedelicPharmacy.item("botanical_capsule_shell")))) ,"Paper formulation does not silently consume a shell");
		h.assertTrue(recipe.results().getFirst().create().getCount()==4&&recipe.time()==180&&recipe.energyPerTick()==96,"Yield, process duration and power remain unchanged");
		h.assertTrue(recipe.tool().orElseThrow().test(new ItemStack(PsychedelicPharmacy.item("lsd_formula_plate"))),"Existing reusable formula remains required");
		h.succeed();
	}
}
