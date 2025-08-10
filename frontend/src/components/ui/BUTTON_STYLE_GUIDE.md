# Button Style Guide

## Button Hierarchy

### Primary Actions (variant="default")
- Main CTA on page (e.g., "Create", "Save", "Submit")
- One primary button per section/form
- Use for the most important action

### Secondary Actions (variant="outline")
- Secondary actions (e.g., "Cancel", "Export", "Filter")
- Multiple allowed per section
- Use for supporting actions

### Tertiary Actions (variant="ghost")
- Low-emphasis actions (e.g., "Learn more", icon buttons in tables)
- Unlimited per section
- Use for optional/supplementary actions

### Destructive Actions (variant="destructive")
- Delete, Remove, or other dangerous actions
- Always require confirmation
- Use sparingly and with clear intent

## Icon Sizes

### Standard Icon Size
- All button icons: `h-4 w-4`
- Always include margin: `mr-2` (for left icons)
- Example: `<Plus className="h-4 w-4 mr-2" />`

### Button Sizes

#### Toolbar/Header Actions
- Size: `size="sm"`
- Use for: Page header actions, toolbar buttons, filters

#### Form Actions
- Size: default (no size prop)
- Use for: Form submit/cancel, modal actions

#### Inline Actions
- Size: `size="sm"` or icon buttons
- Use for: Table row actions, list item actions

## Examples

```tsx
// Page header action
<Button size="sm">
  <Plus className="h-4 w-4 mr-2" />
  Create New
</Button>

// Form actions (using FormActions component)
<FormActions 
  primaryLabel="Save"
  primaryIcon={Save}
  secondaryLabel="Cancel"
/>

// Table row action
<Button variant="ghost" size="sm">
  <Edit2 className="h-4 w-4 mr-2" />
  Edit
</Button>

// Icon-only button (requires aria-label)
<Button variant="ghost" size="icon" aria-label="More actions">
  <MoreHorizontal className="h-4 w-4" />
</Button>
```