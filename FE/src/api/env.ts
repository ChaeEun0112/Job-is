const requireEnv = (key: string, value: string | undefined): string => {
  if (!value) throw new Error(`${key} is not defined. Please check your .env file.`);
  return value;
};

export const ENV = {
  get API_BASE_URL() {
    return requireEnv('VITE_API_BASE_URL', import.meta.env.VITE_API_BASE_URL);
  },
} as const;
