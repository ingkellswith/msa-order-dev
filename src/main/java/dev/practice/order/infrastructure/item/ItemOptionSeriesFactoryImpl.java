package dev.practice.order.infrastructure.item;

import dev.practice.order.domain.item.Item;
import dev.practice.order.domain.item.ItemCommand;
import dev.practice.order.domain.item.ItemOptionSeriesFactory;
import dev.practice.order.domain.item.option.ItemOptionStore;
import dev.practice.order.domain.item.optiongroup.ItemOptionGroup;
import dev.practice.order.domain.item.optiongroup.ItemOptionGroupStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemOptionSeriesFactoryImpl implements ItemOptionSeriesFactory {
    private final ItemOptionGroupStore itemOptionGroupStore;
    private final ItemOptionStore itemOptionStore;

    @Override
    public List<ItemOptionGroup> store(ItemCommand.RegisterItemRequest command, Item item) {
        var itemOptionGroupRequestList = command.getItemOptionGroupRequestList();
        if (CollectionUtils.isEmpty(itemOptionGroupRequestList)) return Collections.emptyList();

        return itemOptionGroupRequestList.stream()
                .map(requestItemOptionGroup -> {
                    // itemOptionGroup store ex)색상, 사이즈
                    var initItemOptionGroup = requestItemOptionGroup.toEntity(item);
                    var itemOptionGroup = itemOptionGroupStore.store(initItemOptionGroup);

                    // itemOption store ex)파랑,노랑,초록,L,XL,XXL 등
                    requestItemOptionGroup.getItemOptionRequestList().forEach(requestItemOption -> {
                        var initItemOption = requestItemOption.toEntity(itemOptionGroup);
                        itemOptionStore.store(initItemOption);
                    });

                    return itemOptionGroup;
                }).collect(Collectors.toList());
    }
}
