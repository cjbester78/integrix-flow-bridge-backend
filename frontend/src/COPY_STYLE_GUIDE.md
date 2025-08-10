# Copy Style Guide

## General Principles

1. **Be Concise**: Use clear, simple language
2. **Be Consistent**: Use the same terminology throughout
3. **Be Action-Oriented**: Use verbs for actions
4. **Be User-Focused**: Write from the user's perspective

## Capitalization

### Title Case
Use for:
- Page titles (e.g., "Integration Dashboard")
- Section headers (e.g., "File Access Parameters")
- Button labels (e.g., "Create Structure")
- Menu items (e.g., "Data Structures")

### Sentence Case
Use for:
- Descriptions and helper text
- Error messages
- Toast notifications
- Form field labels

## Common Terms

### Correct Usage
- **Integration Flow** (not "flow" or "integration")
- **Communication Adapter** (not "adapter" alone)
- **Data Structure** (not "structure")
- **Business Component** (not "component")
- **Source/Target** (not "sender/receiver" in UI)
- **Transform/Passthrough** (not "transformation/pass-through")

### Pluralization
- Integration Flows
- Communication Adapters
- Data Structures
- Business Components
- Messages (not "Message List")
- Settings (not "Setting")

## Action Labels

### Primary Actions
- **Create** (not "Add" or "New")
- **Save** (not "Submit" or "Update")
- **Delete** (not "Remove")
- **Edit** (not "Modify")
- **Deploy** (not "Activate")

### Secondary Actions
- **Cancel** (not "Close" or "Back")
- **Export** (not "Download")
- **Import** (not "Upload")
- **Refresh** (not "Reload")
- **Filter** (not "Search" when filtering)

## Status Labels

### Flow Status
- **Active** (running)
- **Inactive** (stopped)
- **Deployed** (ready to run)
- **Draft** (not deployed)
- **Error** (failed state)

### Message Status
- **Success** (completed successfully)
- **Failed** (error occurred)
- **Processing** (in progress)
- **Pending** (waiting to start)

## Error Messages

### Format
- Start with what happened: "Failed to..."
- Be specific: "Failed to load data structures"
- Offer next steps when possible: "Please try again or contact support"

### Examples
- ✅ "Failed to create integration flow. Please check your configuration."
- ❌ "Error occurred"
- ✅ "Connection test failed. Please verify your credentials."
- ❌ "Test failed"

## Empty States

### Format
- Title: State what's missing
- Description: Brief explanation or next step
- Action: Clear CTA if applicable

### Examples
```
Title: No data structures found
Description: Create your first data structure to define message formats
Action: Create Structure
```

## Confirmation Dialogs

### Delete Confirmations
```
Title: Delete [Item Type]?
Description: Are you sure you want to delete "[Item Name]"? This action cannot be undone.
Actions: Cancel | Delete
```

### Deploy Confirmations
```
Title: Deploy Integration Flow?
Description: This will deploy "[Flow Name]" to the selected environment.
Actions: Cancel | Deploy
```

## Navigation Labels

### Main Menu
- Dashboard
- All Interfaces
- Communication Adapters
- Data Structures
- Business Components
- Messages
- Settings
- Admin (if applicable)

### Breadcrumbs
- Use full names, not abbreviations
- Match page titles exactly
- Example: Home > Data Structures > Create Structure