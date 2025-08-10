import * as React from "react"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { cn } from "@/lib/utils"
import { Eye, EyeOff, AlertCircle, CheckCircle } from "lucide-react"
import { validateAdapterPassword, PasswordValidationResult } from "@/utils/passwordValidation"

interface PasswordConfirmationProps {
  name: string
  label: string
  placeholder?: string
  required?: boolean
  value: string
  onValueChange: (value: string) => void
  className?: string
  showConfirmation?: boolean
  validator?: (password: string) => PasswordValidationResult
}

export const PasswordConfirmation = React.forwardRef<
  HTMLDivElement,
  PasswordConfirmationProps
>(({ name, label, placeholder, required, value, onValueChange, className, showConfirmation = true, validator = validateAdapterPassword }, ref) => {
  const [password, setPassword] = React.useState(value || "")
  const [confirmPassword, setConfirmPassword] = React.useState("")
  const [showPassword, setShowPassword] = React.useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = React.useState(false)
  const [hasTypedConfirm, setHasTypedConfirm] = React.useState(false)
  const [validation, setValidation] = React.useState<PasswordValidationResult>({ isValid: true, errors: [] })

  // Sync with external value changes
  React.useEffect(() => {
    if (value !== password) {
      setPassword(value || "")
    }
  }, [value])

  const isPasswordMatch = password === confirmPassword
  const showError = hasTypedConfirm && confirmPassword.length > 0 && !isPasswordMatch
  const showValidationError = !validation.isValid && password.length > 0

  const handlePasswordChange = (newPassword: string) => {
    setPassword(newPassword)
    setValidation(validator(newPassword))
    onValueChange(newPassword)
  }

  const handleConfirmPasswordChange = (newConfirmPassword: string) => {
    setConfirmPassword(newConfirmPassword)
    if (!hasTypedConfirm && newConfirmPassword.length > 0) {
      setHasTypedConfirm(true)
    }
  }

  
  return (
    <div ref={ref} className={cn("space-y-3", className)}>
      <div className="space-y-2">
        <Label htmlFor={name} className="flex items-center gap-1">
          {label}
          {required && <span className="text-destructive">*</span>}
        </Label>
        <div className="relative">
          <Input
            id={name}
            type={showPassword ? "text" : "password"}
            placeholder={placeholder}
            value={password}
            onChange={(e) => handlePasswordChange(e.target.value)}
            className={cn(
              "pr-10 transition-all duration-300 focus:scale-[1.01]",
              showValidationError && "border-destructive focus-visible:ring-destructive"
            )}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
          >
            {showPassword ? (
              <EyeOff className="h-4 w-4" />
            ) : (
              <Eye className="h-4 w-4" />
            )}
          </button>
        </div>
      </div>

      {/* Password validation feedback */}
      {showValidationError && (
        <div className="space-y-1">
          {validation.errors.map((error, index) => (
            <div key={index} className="flex items-center gap-2 text-sm text-destructive">
              <AlertCircle className="h-4 w-4" />
              {error}
            </div>
          ))}
        </div>
      )}

      {validation.isValid && password.length > 0 && (
        <div className="flex items-center gap-2 text-sm text-green-600">
          <CheckCircle className="h-4 w-4" />
          Password meets requirements
        </div>
      )}

      {showConfirmation && (
        <div className="space-y-2">
          <div className="space-y-2">
            <Label htmlFor={`${name}_confirm`} className="flex items-center gap-1">
              Confirm {label}
              {required && <span className="text-destructive">*</span>}
            </Label>
            <div className="relative">
              <Input
                id={`${name}_confirm`}
                type={showConfirmPassword ? "text" : "password"}
                placeholder={`Confirm ${placeholder || label.toLowerCase()}`}
                value={confirmPassword}
                onChange={(e) => handleConfirmPasswordChange(e.target.value)}
                className={cn(
                  "pr-10 transition-all duration-300 focus:scale-[1.01]",
                  showError && "border-destructive focus-visible:ring-destructive"
                )}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              >
                {showConfirmPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
          </div>

          {showError && (
            <div className="flex items-center gap-2 text-sm text-destructive">
              <AlertCircle className="h-4 w-4" />
              Passwords do not match
            </div>
          )}
        </div>
      )}
    </div>
  )
})

PasswordConfirmation.displayName = "PasswordConfirmation"