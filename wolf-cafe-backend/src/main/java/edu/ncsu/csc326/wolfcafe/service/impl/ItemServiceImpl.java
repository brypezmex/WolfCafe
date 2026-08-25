package edu.ncsu.csc326.wolfcafe.service.impl;

import edu.ncsu.csc326.wolfcafe.dto.ItemDto;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.service.ItemService;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implemented item service
 */
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

	/** Item repository */
    private ItemRepository itemRepository;

    /** Mapper class */
    private ModelMapper modelMapper;

    /** Screens user-supplied text for profanity */
    private ProfanityFilterService profanityFilterService;

    /**
	 * Adds given item
	 * @param itemDto item to add
	 * @return added item
	 */
    @Override
    public ItemDto addItem(ItemDto itemDto) {
        validateLanguage(itemDto);
        Item item = modelMapper.map(itemDto, Item.class);
        Item savedItem = itemRepository.save(item);
        return modelMapper.map(savedItem, ItemDto.class);
    }

    /**
     * Gets item by id
     * @param id id of item to get
     * @return returned item
     */
    @Override
    public ItemDto getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
        return modelMapper.map(item, ItemDto.class);
    }

    /**
     * Returns all items
     * @return all items
     */
    @Override
    public List<ItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return items.stream().map((item) -> modelMapper.map(item, ItemDto.class)).collect(Collectors.toList());
    }

    /**
     * Updates the item with the given id
     * @param id id of item to update
     * @param itemDto information of item to update
     * @return updated item
     */
    @Override
    public ItemDto updateItem(Long id, ItemDto itemDto) {
        validateLanguage(itemDto);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setPrice(itemDto.getPrice());
        Item updatedItem = itemRepository.save((item));
        return modelMapper.map(updatedItem, ItemDto.class);
    }

    /**
     * Screens the item's free-text fields for profanity.
     * @param itemDto item whose text should be screened
     */
    private void validateLanguage(ItemDto itemDto) {
        profanityFilterService.validate("Item name", itemDto.getName());
        profanityFilterService.validate("Item description", itemDto.getDescription());
    }

    /**
     * Deletes the item with the given id
     * @param id id of item to delete
     */
    @Override
    public void deleteItem(Long id) {
        itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id " + id));
        itemRepository.deleteById(id);
    }
}
